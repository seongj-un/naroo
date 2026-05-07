# Naroo Backend Railway Deploy

Last updated: 2026-05-06

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
- Railway config-as-code 파일 [railway.toml](/Users/seongjun/Desktop/project/naroo/railway.toml) 추가
- GitHub Actions 테스트 워크플로 [.github/workflows/backend-test.yml](/Users/seongjun/Desktop/project/naroo/.github/workflows/backend-test.yml) 추가
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
- `NAROO_AUTH_REFRESH_COOKIE_SAME_SITE=Strict`
- `NAROO_JWT_ACCESS_TOKEN_TTL_MINUTES=60`
- `NAROO_JWT_REFRESH_TOKEN_TTL_DAYS=3`
- `NAROO_AUTH_EMAIL_VERIFICATION_TOKEN_TTL_MINUTES=30`
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
- `SameSite=Strict`

선택 기준:

- 프론트와 백엔드가 같은 사이트(`naroo.app`, `api.naroo.app`)면 `Strict` 유지
- 프론트와 백엔드가 서로 다른 사이트면 `NAROO_AUTH_REFRESH_COOKIE_SAME_SITE=None`으로 바꾸고 `Secure=true` 유지

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
   - 필요 시 `NAROO_AUTH_REFRESH_COOKIE_SAME_SITE=None`
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
- 응답에 `refresh_token` 쿠키 포함
- 프론트 origin에서 `POST /api/auth/reissue`가 cookie 포함으로 성공

## GitHub Actions

현재 저장소에는 테스트 워크플로만 추가했다.

- PR / main push 시 `./gradlew test`

지금 단계에서 Railway deploy workflow를 같이 넣지 않은 이유:

- 실제 Railway project/service 연결 정보가 이 저장소 안에 없다
- 잘못된 deploy workflow를 넣는 것보다 test gate만 먼저 두는 편이 안전하다

향후 CLI 배포를 붙일 때 필요한 대표 secret:

- `RAILWAY_TOKEN`

## 남아 있는 블로커

- 실제 Railway project와 backend service가 아직 만들어지지 않았다
- 실제 frontend 배포 origin이 아직 확정되지 않았다
  - 이 값이 확정되어야 `NAROO_CORS_ALLOWED_ORIGINS`와 `NAROO_AUTH_REFRESH_COOKIE_SAME_SITE`를 최종 결정할 수 있다
- Railway 상의 MySQL/Redis reference vars가 실제로 연결되었는지는 이 저장소 안에서 검증할 수 없다
- 현재 로컬 환경에는 Docker가 없어서 MySQL/Redis 포함한 완전한 prod-like 부팅 검증은 못 했다

## 프론트 저장소에 넘길 값

최종 형식:

```text
NAROO_API_BASE_URL=https://<backend-domain>
```

예시:

```text
NAROO_API_BASE_URL=https://naroo-backend-production.up.railway.app
```
