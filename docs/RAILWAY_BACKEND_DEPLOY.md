# Naroo Backend Railway Deploy

Last updated: 2026-05-26

이 문서는 현재 `naroo` 백엔드 코드 기준으로 Railway 첫 배포에 필요한 최소 조건만 정리한다. 추측성 설정은 넣지 않고, 실제 코드가 읽는 값과 이번 저장소 변경만 다룬다.

## 현재 코드 기준 배포 조건

- Java 17
- Spring Boot 3.4.x
- MySQL 필수
- Redis 필수
- JWT secret 필수
- CORS allowed origin 명시 필수
- Railway `PORT` 바인딩 필수
- HTTP health check 필수

현재 코드는 Flyway migration을 앱 기동 시 실행한다. 그래서 MySQL 연결이 안 되면 Railway 배포가 health check 전에 실패한다.

## 이번에 반영한 Railway 대응

- `server.port=${PORT:8080}` 추가
- Railway MySQL 변수 직접 지원
  - `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE`, `MYSQL_URL`
- Railway Redis 변수 직접 지원
  - `REDISHOST`, `REDISPORT`, `REDISUSER`, `REDISPASSWORD`
- Spring Boot Actuator 추가
- `/actuator/health` 노출
- Railway config-as-code 파일 [railway.toml](../railway.toml) 추가
- GitHub Actions 테스트 워크플로 `.github/workflows/backend-test.yml` 추가
- GitHub Actions Railway 배포 워크플로 `.github/workflows/backend-deploy-railway.yml` 추가
- `prod` 프로필에서 운영 안전장치 추가
  - 기본 JWT secret 금지
  - `NAROO_AUTH_REFRESH_COOKIE_SECURE=true` 강제
  - `NAROO_CORS_ALLOWED_ORIGINS`에 localhost 금지
  - `NAROO_CORS_ALLOWED_ORIGINS`는 https만 허용

## Health Check 전략

Actuator로 간다.

이유:

- Railway는 배포 시 HTTP 200 health check를 기다린다.
- 이 백엔드는 실제 배포 성공 판단에 DB/MySQL과 Redis 연결 상태가 중요하다.
- 기존 API endpoint를 재사용하면 인증, 도메인 로직, 응답 wrapper까지 섞여서 health 목적이 흐려진다.
- Actuator health endpoint는 Spring 표준 방식이라 Railway health check에 바로 붙이기 쉽다.

현재 구현:

- actuator health: `/actuator/health`
- Railway healthcheck path: `/actuator/health`

현재 앱에서는 first deploy 기준으로 별도 liveness/readiness path를 억지로 늘리기보다, 실제로 검증된 actuator health 하나를 쓰는 편이 낫다. 이 endpoint도 DB/MySQL과 Redis health indicator를 포함하므로, 둘 중 하나가 죽어 있으면 Railway healthcheck는 200을 받지 못한다.

## 운영 환경변수

### 반드시 직접 설정해야 하는 값

- `SPRING_PROFILES_ACTIVE=prod`
- `NAROO_JWT_SECRET`
- `NAROO_CORS_ALLOWED_ORIGINS`

### 보통 직접 설정하는 값

- `NAROO_AUTH_REFRESH_COOKIE_SECURE=true`
- `NAROO_AUTH_REFRESH_COOKIE_SAME_SITE=None`
- `NAROO_JWT_ACCESS_TOKEN_TTL_MINUTES=60`
- `NAROO_JWT_REFRESH_TOKEN_TTL_DAYS=3`
- `NAROO_AUTH_EMAIL_VERIFICATION_TOKEN_TTL_MINUTES=30`
- `NAROO_SEED_BETA_CONTENT_ENABLED=true`
- `NAROO_SEED_STUDENT_ENABLED=false`

### MySQL 연결 값

다음 둘 중 하나만 맞으면 된다.

1. Railway MySQL 서비스 연결
   - Railway가 자동 주입:
   - `MYSQL_URL`
   - `MYSQLHOST`
   - `MYSQLPORT`
   - `MYSQLUSER`
   - `MYSQLPASSWORD`
   - `MYSQLDATABASE`

2. 수동 주입
   - `MYSQL_URL`
   - `MYSQL_USER`
   - `MYSQL_PASS`

참고:

- 코드 fallback은 `MYSQL_URL` 우선이다.
- `MYSQL_URL`이 없으면 `MYSQLHOST`/`MYSQLPORT`/`MYSQLDATABASE` 조합으로 JDBC URL을 만든다.
- Railway의 MySQL reference variable은 raw URL일 수 있다. Spring datasource에는 `jdbc:`로 시작하는 값이 필요하므로, raw URL을 `MYSQL_URL`에 그대로 넣지 말고 `MYSQLHOST`/`MYSQLPORT`/`MYSQLDATABASE` 조합을 쓰는 편이 안전하다.

### Redis 연결 값

다음 둘 중 하나만 맞으면 된다.

1. Railway Redis 서비스 연결
   - Railway가 자동 주입:
   - `REDISHOST`
   - `REDISPORT`
   - `REDISUSER`
   - `REDISPASSWORD`

2. 수동 주입
   - `NAROO_REDIS_HOST`
   - `NAROO_REDIS_PORT`
   - `NAROO_REDIS_USERNAME`
   - `NAROO_REDIS_PASSWORD`

## refresh cookie 운영 기준

기본값:

- `HttpOnly`
- `Secure=true`
- `Path=/api/auth`
- `SameSite=None` if frontend stays on `https://naroo.app` and backend stays on a Railway-provided domain

선택 기준:

- 프론트와 백엔드가 같은 사이트(`naroo.app`, `api.naroo.app`)면 `Strict` 유지
- 프론트와 백엔드가 서로 다른 사이트면 `NAROO_AUTH_REFRESH_COOKIE_SAME_SITE=None`으로 바꾸고 `Secure=true` 유지
- 현재 배포 URL이 `https://backend-production-688a6.up.railway.app` 이므로 `https://naroo.app` 프론트와는 cross-site다. 이 조합에서는 `None`이 맞다.

프론트가 cross-site 요청에서 refresh cookie를 써야 하면 `fetch(..., { credentials: "include" })`가 필요하다.

## Railway 배포 순서

1. Railway에 backend service 생성
2. 같은 project에 MySQL service 추가
3. 같은 project에 Redis service 추가
4. backend service에 public domain 부여
5. backend service 변수 설정
   - `SPRING_PROFILES_ACTIVE=prod`
   - `NAROO_JWT_SECRET=<32바이트 이상 랜덤 문자열>`
   - `NAROO_CORS_ALLOWED_ORIGINS=https://<frontend-domain>`
   - `NAROO_AUTH_EMAIL_MODE=smtp`
   - `NAROO_AUTH_EMAIL_FROM_ADDRESS=no-reply@naroo.app`
   - `NAROO_AUTH_EMAIL_FROM_NAME=Naroo`
   - `NAROO_AUTH_EMAIL_VERIFICATION_URL_TEMPLATE=https://naroo.app/verify-email?token={token}`
   - `SPRING_MAIL_HOST=<smtp-host>`
   - `SPRING_MAIL_PORT=<smtp-port>`
   - `SPRING_MAIL_USERNAME=<smtp-username>`
   - `SPRING_MAIL_PASSWORD=<smtp-password>`
   - Railway 기본 도메인을 쓰면 `NAROO_AUTH_REFRESH_COOKIE_SAME_SITE=None`
6. Railway가 MySQL/Redis reference vars를 backend에 연결했는지 확인
7. GitHub repo 연결 후 deploy
8. healthcheck path가 `/actuator/health`로 잡히는지 확인

## 배포 후 확인

브라우저 또는 curl:

```bash
curl https://<backend-domain>/actuator/health
```

기대 결과:

- `/actuator/health` -> 200

추가 확인:

- `POST /api/auth/login` 성공
- `POST /api/auth/sign-up` 후 실제 메일 수신 성공
- 응답에 `refresh_token` 쿠키 포함
- `Set-Cookie`에 `SameSite=None; Secure` 포함
- 프론트 origin에서 `POST /api/auth/reissue`가 cookie 포함으로 성공

## GitHub Actions

현재 저장소에는 두 가지 워크플로가 있다.

- `.github/workflows/backend-test.yml`
  - PR과 수동 실행에서 `./gradlew test`
- `.github/workflows/backend-deploy-railway.yml`
  - `main` push 또는 수동 실행에서 `./gradlew test`
  - `RAILWAY_API_TOKEN`, `RAILWAY_PROJECT_ID`, `RAILWAY_ENVIRONMENT_NAME`, `RAILWAY_SERVICE_NAME`가 모두 있으면 `railway up --ci`로 배포

배포 워크플로에 필요한 대표 값:

- `RAILWAY_API_TOKEN`
- `RAILWAY_PROJECT_ID`
- `RAILWAY_ENVIRONMENT_NAME`
- `RAILWAY_SERVICE_NAME`

## 운영 메모

- `prod` 프로필에서는 seeded QA 학생 계정이 더 이상 생성되지 않는다.
- 운영 QA가 필요하면 실제 수신 가능한 메일함 계정으로 가입해서 검증하는 편이 맞다.
- 기존 임시 QA 계정이 운영 DB에 남아 있다면 별도 수동 정리가 필요하다.

## 남아 있는 블로커

- 실제 frontend 배포 origin이 바뀌면 `NAROO_CORS_ALLOWED_ORIGINS`와 `NAROO_AUTH_REFRESH_COOKIE_SAME_SITE`도 같이 재검토해야 한다
- 최종 운영 구조는 `https://api.naroo.app` 같은 same-site 도메인으로 옮기는 편이 더 안전하다
- 현재 로컬 환경에는 Docker가 없어서 MySQL/Redis 포함한 완전한 prod-like 부팅 검증은 못 했다

## 프론트 저장소에 넘길 값

최종 형식:

```text
NAROO_API_BASE_URL=https://<backend-domain>
```

예시:

```text
NAROO_API_BASE_URL=https://backend-production-688a6.up.railway.app
```
