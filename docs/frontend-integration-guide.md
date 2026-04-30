지금# Naroo Frontend Integration Guide

Last updated: 2026-04-30

이 문서는 프론트 개발자가 백엔드 API를 연동할 때 반드시 알아야 하는 계약, 화면 흐름, 주의사항을 한 파일에 모은 것이다. 현재 백엔드는 학습자 가입/로그인, 이메일 인증, 진단, 회복 미션, 학습 홈, 콘텐츠 관리 API까지 구현되어 있다.

## 가장 중요한 판단

프론트 개발을 바로 시작해도 되지만, 아래 두 가지는 먼저 결정해야 한다.

1. **CORS**
   - 백엔드에 명시적인 CORS 설정이 추가되어 있다.
   - 기본 허용 origin은 `http://localhost:3000`, `http://localhost:5173`이다.
   - 추가 origin은 `NAROO_CORS_ALLOWED_ORIGINS`로 콤마 구분 설정한다.
   - refresh cookie를 쓰기 위해 `credentials: true` 정책을 사용하므로 allowed origin에 `*`는 쓰지 않는다.

2. **refresh token cookie**
   - 로그인/재발급 응답은 `refresh_token` 쿠키를 내려준다.
   - 쿠키 옵션은 `HttpOnly`, `Secure`, `SameSite=Strict`, `Path=/api/auth`이다.
   - 운영 기본값은 `Secure=true`이다.
   - 로컬 HTTP 개발에서는 `NAROO_AUTH_REFRESH_COOKIE_SECURE=false`로 실행한다.

추가로, `GET /api/contents/diagnostic-questions`는 `correctChoiceId`를 포함한다. 학생용 진단 화면에서 이 API를 쓰면 정답이 노출된다. 학생용 화면은 반드시 `GET /api/diagnostics/{diagnosticSessionId}/questions`만 사용해야 한다.

## 실행 환경

기본 백엔드 주소는 별도 설정이 없으면 `http://localhost:8080`이다.

Backend start:

```bash
./gradlew bootRun
```

Backend test:

```bash
./gradlew test
```

기본 설정은 [application.yaml](../src/main/resources/application.yaml)에 있다.

Required local services:

- MySQL: default `jdbc:mysql://localhost:3306/naroo`
- MySQL user/password: default `naroo` / `naroo`
- Redis: default `localhost:6379`

Environment variables:

- `MYSQL_URL` or `NAROO_DATABASE_URL`
- `MYSQL_USER` or `NAROO_DATABASE_USERNAME`
- `MYSQL_PASS` or `NAROO_DATABASE_PASSWORD`
- `NAROO_REDIS_HOST`
- `NAROO_REDIS_PORT`
- `NAROO_CORS_ALLOWED_ORIGINS`
- `NAROO_AUTH_REFRESH_COOKIE_SECURE`
- `NAROO_JWT_SECRET`
- `NAROO_JWT_ACCESS_TOKEN_TTL_MINUTES`
- `NAROO_JWT_REFRESH_TOKEN_TTL_DAYS`

## 공통 응답 형식

모든 성공 응답은 wrapper를 사용한다.

```json
{
  "success": true,
  "data": {}
}
```

에러 응답:

```json
{
  "success": false,
  "data": {
    "errorCode": "GLOBAL_VALIDATION_ERROR"
  }
}
```

프론트에서는 HTTP status와 `data.errorCode`를 같이 봐야 한다.

대표 에러코드:

- Auth: `AUTH_LOGIN_ID_ALREADY_EXISTS`, `AUTH_EMAIL_ALREADY_EXISTS`, `AUTH_INVALID_CREDENTIALS`, `AUTH_INVALID_REFRESH_TOKEN`, `AUTH_INVALID_EMAIL_VERIFICATION_TOKEN`, `AUTH_REFRESH_TOKEN_REQUIRED`, `AUTH_UNAUTHORIZED`
- Diagnostic: `DIAGNOSTIC_EMAIL_VERIFICATION_REQUIRED`, `DIAGNOSTIC_STARTING_POINT_SELECTION_REQUIRED`, `DIAGNOSTIC_SESSION_NOT_FOUND`, `DIAGNOSTIC_QUESTIONS_NOT_FOUND`, `DIAGNOSTIC_INVALID_ANSWER`, `DIAGNOSTIC_ALREADY_COMPLETED`, `DIAGNOSTIC_RESULT_NOT_READY`
- Recovery: `RECOVERY_DIAGNOSTIC_RESULT_REQUIRED`, `RECOVERY_MISSION_NOT_FOUND`, `RECOVERY_MISSION_ALREADY_COMPLETED`, `RECOVERY_INVALID_MISSION_SUBMISSION`, `RECOVERY_MISSION_TEMPLATE_NOT_FOUND`
- Global: `GLOBAL_VALIDATION_ERROR`, `GLOBAL_METHOD_NOT_ALLOWED`, `GLOBAL_BAD_REQUEST`, `GLOBAL_NOT_FOUND`, `GLOBAL_UNAUTHORIZED`

## 인증 정책

로그인 성공 시 response body에는 access token이 있고, response header에는 refresh cookie가 있다.

Access token은 모든 인증 API 호출에 아래처럼 붙인다.

```http
Authorization: Bearer {accessToken}
```

권장 프론트 보관 방식:

- access token: memory state 또는 짧은 수명의 app store
- refresh token: JS에서 접근하지 않음. 브라우저 cookie가 자동 처리해야 한다.
- 재발급 API 호출 시 `credentials: "include"` 필요
- 로컬 HTTP에서 refresh cookie를 검증하려면 백엔드 실행 환경에 `NAROO_AUTH_REFRESH_COOKIE_SECURE=false` 필요

Example fetch:

```ts
await fetch(`${API_BASE_URL}/api/me/learning-home`, {
  headers: {
    Authorization: `Bearer ${accessToken}`,
  },
});
```

Refresh example:

```ts
await fetch(`${API_BASE_URL}/api/auth/reissue`, {
  method: "POST",
  credentials: "include",
});
```

주의:

- `/api/auth/login`의 `user.role`과 `/api/auth/me`의 `role`을 사용한다.
- 프론트는 JWT payload를 직접 디코딩하지 않는다.
- 관리자 화면 접근 판단은 `role === "ADMIN"` 기준으로 한다.

## 사용자 플로우

프론트의 첫 화면 판단은 `GET /api/me/learning-home`을 기준으로 하는 것이 가장 단순하다.

1. 회원가입
2. 로그인
3. access token 저장
4. 앱 부팅 시 learning home 호출
5. `nextAction`에 따라 화면 분기

`nextAction` 값:

- `EMAIL_VERIFICATION_REQUIRED`: 이메일 인증 필요 화면
- `START_DIAGNOSTIC`: 진단 시작 화면
- `CREATE_RECOVERY_MISSION`: 진단 결과 기반 미션 생성 CTA
- `CONTINUE_RECOVERY_MISSION`: 진행 중 미션 이어하기

권장 화면 순서:

1. Sign up / Login
2. Email verification
3. Learning home
4. Starting point selection
5. Diagnostic questions
6. Diagnostic result
7. Recovery mission
8. Mission submission feedback
9. Learning home refresh

## Auth API

### POST `/api/auth/sign-up`

Request:

```json
{
  "loginId": "student01",
  "email": "student@example.com",
  "password": "password1234",
  "nickname": "나루",
  "mathStatus": "UNKNOWN"
}
```

`mathStatus`:

- `FOLLOWS_CLASS`
- `BARELY_FOLLOWS`
- `MOSTLY_GAVE_UP`
- `UNKNOWN`

Validation:

- `loginId`: 4-30자, 영문/숫자/`.`/`_`/`-`
- `email`: 이메일 형식, 최대 254자
- `nickname`: 2-20자

Response data:

```json
{
  "id": "user-id",
  "loginId": "student01",
  "email": "student@example.com",
  "emailVerified": false,
  "nickname": "나루",
  "mathStatus": "UNKNOWN",
  "createdAt": "2026-04-30T00:00:00Z"
}
```

### POST `/api/auth/login`

Request:

```json
{
  "loginId": "student01",
  "password": "password1234"
}
```

Response data:

```json
{
  "accessToken": "jwt",
  "tokenType": "Bearer",
  "expiresAt": "2026-04-30T01:00:00Z",
  "user": {
    "id": "user-id",
    "loginId": "student01",
    "email": "student@example.com",
    "emailVerified": true,
    "nickname": "나루",
    "mathStatus": "UNKNOWN",
    "role": "STUDENT"
  }
}
```

### POST `/api/auth/reissue`

Uses `refresh_token` cookie.

Response data:

```json
{
  "accessToken": "new-jwt",
  "tokenType": "Bearer",
  "expiresAt": "2026-04-30T02:00:00Z"
}
```

### POST `/api/auth/email/verify`

Request:

```json
{
  "token": "email-verification-token"
}
```

Response data:

```json
{
  "userId": "user-id",
  "email": "student@example.com",
  "emailVerified": true
}
```

현재 local email sender는 실제 메일 발송이 아니라 로그 기반이다. 로컬 개발에서는 서버 로그에서 token을 확인하거나, 별도 dev helper를 만들 필요가 있다.

### GET `/api/auth/me`

Requires Bearer token.

Response data:

```json
{
  "id": "user-id",
  "loginId": "student01",
  "emailVerified": true,
  "nickname": "나루",
  "role": "STUDENT"
}
```

## Learning Home API

### GET `/api/me/learning-home`

Requires Bearer token.

Response data:

```json
{
  "user": {
    "id": "user-id",
    "nickname": "나루",
    "emailVerified": true
  },
  "nextAction": "START_DIAGNOSTIC",
  "latestDiagnostic": null,
  "todayMission": null,
  "progress": {
    "completedMissionCount": 0,
    "inProgressMissionCount": 0
  }
}
```

이 API는 홈 화면뿐 아니라 앱 부팅 후 라우팅 기준으로 써도 된다.

## Math Area API

### GET `/api/math-areas`

Public API.

Response data:

```json
[
  {
    "code": "EQUATION",
    "name": "방정식",
    "description": "식 정리, 등식 변형, 해를 구하는 과정이 헷갈리는 경우",
    "recommendedFor": "문제는 보이지만 식을 세우거나 풀 때 자주 막히는 학생",
    "displayOrder": 1
  }
]
```

`code` values:

- `EQUATION`
- `FUNCTION`
- `GEOMETRY`
- `PROBABILITY_AND_STATISTICS`
- `SEQUENCE`

## Diagnostic API

All diagnostic APIs require Bearer token and verified email.

### POST `/api/diagnostics/starting-point`

Request:

```json
{
  "selectionType": "WEAK_AREA",
  "mathArea": "FUNCTION",
  "note": "그래프 문제가 어려워요"
}
```

`selectionType`:

- `WEAK_AREA`
- `STUDY_INTEREST`

Response data includes `id`, `userId`, `selectionType`, `mathArea`, `note`, `createdAt`, `updatedAt`.

### POST `/api/diagnostics`

Creates a diagnostic session from the user's latest starting point.

No request body.

Response data:

```json
{
  "id": "diagnostic-session-id",
  "userId": "user-id",
  "startingPointSelectionId": "starting-point-id",
  "mathArea": "FUNCTION",
  "questionSnapshotVersion": 1,
  "status": "READY",
  "createdAt": "2026-04-30T00:00:00Z",
  "updatedAt": "2026-04-30T00:00:00Z"
}
```

`status`:

- `READY`
- `IN_PROGRESS`
- `COMPLETED`

중요:

- 세션 생성 시점의 active question이 snapshot으로 저장된다.
- 이후 콘텐츠가 수정되어도 이미 만들어진 세션의 문항은 바뀌지 않는다.

### GET `/api/diagnostics/{diagnosticSessionId}/questions`

Response data:

```json
{
  "diagnosticSessionId": "diagnostic-session-id",
  "mathArea": "FUNCTION",
  "status": "IN_PROGRESS",
  "questions": [
    {
      "id": "question-id",
      "prompt": "문제 내용",
      "choices": [
        {
          "id": "a",
          "text": "선택지 A"
        }
      ]
    }
  ]
}
```

프론트 구현 기준:

- 이 응답에는 정답이 없다.
- 학생 진단 화면은 이 API만 사용한다.
- 선택지 id는 서버 응답을 그대로 사용한다.
- `unknown` 선택지가 오면 "잘 모르겠음" 같은 라벨로 보여주면 된다.

### POST `/api/diagnostics/{diagnosticSessionId}/answers`

Request:

```json
{
  "answers": [
    {
      "questionId": "question-id",
      "selectedChoiceId": "a"
    }
  ]
}
```

주의:

- 세션의 모든 문항에 대해 정확히 한 번씩 답변해야 한다.
- 누락, 중복, 존재하지 않는 choice id는 `DIAGNOSTIC_INVALID_ANSWER`로 실패한다.
- 이미 완료된 세션에 다시 제출하면 `DIAGNOSTIC_ALREADY_COMPLETED`가 난다.

Response data:

```json
{
  "diagnosticSessionId": "diagnostic-session-id",
  "mathArea": "FUNCTION",
  "status": "COMPLETED",
  "totalQuestionCount": 5,
  "correctCount": 2,
  "wrongCount": 2,
  "unknownCount": 1,
  "weakLinks": ["linear-function"],
  "primaryRecoveryConcept": "linear-function",
  "summary": "함수 개념 연결이 약해요"
}
```

### GET `/api/diagnostics/{diagnosticSessionId}/result`

Requires completed diagnostic.

Response data:

```json
{
  "diagnosticSessionId": "diagnostic-session-id",
  "mathArea": "FUNCTION",
  "status": "COMPLETED",
  "totalQuestionCount": 5,
  "correctCount": 2,
  "wrongCount": 2,
  "unknownCount": 1,
  "weakLinks": ["linear-function"],
  "primaryRecoveryConcept": "linear-function",
  "summary": "함수 개념 연결이 약해요",
  "nextMissionPreview": {
    "conceptTag": "linear-function",
    "title": "일차함수 회복 미션",
    "estimatedMinutes": 10,
    "tone": "차근차근 다시 연결해보자"
  }
}
```

## Recovery Mission API

All recovery mission APIs require Bearer token and verified email.

### POST `/api/recovery-missions`

Request:

```json
{
  "diagnosticSessionId": "diagnostic-session-id"
}
```

Behavior:

- 진단 결과가 있어야 한다.
- 진행 중인 미션이 있으면 같은 미션을 반환한다.
- 미션 완료 후 다시 호출하면 다음 weak link 기반 미션을 만든다.
- 더 만들 미션이 없으면 최근 완료 미션을 반환할 수 있다.

Response data:

```json
{
  "id": "mission-id",
  "diagnosticSessionId": "diagnostic-session-id",
  "conceptTag": "linear-function",
  "title": "일차함수 회복 미션",
  "prompt": "미션 설명",
  "hints": ["힌트 1", "힌트 2"],
  "status": "IN_PROGRESS",
  "estimatedMinutes": 10,
  "createdAt": "2026-04-30T00:00:00Z",
  "completedAt": null
}
```

`status`:

- `IN_PROGRESS`
- `COMPLETED`

### GET `/api/recovery-missions/{recoveryMissionId}`

Returns one mission.

### POST `/api/recovery-missions/{recoveryMissionId}/submissions`

Request:

```json
{
  "answerText": "제가 생각한 풀이 과정입니다."
}
```

Response data:

```json
{
  "id": "submission-id",
  "recoveryMissionId": "mission-id",
  "feedbackTitle": "좋아요",
  "feedbackMessage": "피드백 내용",
  "nextAction": "다음 미션으로 이어가세요",
  "submittedAt": "2026-04-30T00:00:00Z",
  "mission": {
    "id": "mission-id",
    "diagnosticSessionId": "diagnostic-session-id",
    "conceptTag": "linear-function",
    "title": "일차함수 회복 미션",
    "prompt": "미션 설명",
    "hints": ["힌트 1"],
    "status": "COMPLETED",
    "estimatedMinutes": 10,
    "createdAt": "2026-04-30T00:00:00Z",
    "completedAt": "2026-04-30T00:00:00Z"
  }
}
```

일반 UX에서는 `/complete`보다 `/submissions`를 사용한다. `/complete`는 답변 제출 없이 완료 처리하는 API라서 운영 화면의 기본 동선에는 어울리지 않는다.

## Content API

콘텐츠 API는 관리자/운영 도구 성격이다.

### GET `/api/contents/diagnostic-questions`

Public API이지만 정답인 `correctChoiceId`를 포함한다. 학생 앱에서 호출하지 말 것.

Response data:

```json
{
  "questions": [
    {
      "id": "function-001",
      "mathArea": "FUNCTION",
      "prompt": "문제 내용",
      "correctChoiceId": "a",
      "conceptTag": "linear-function",
      "displayOrder": 1,
      "status": "ACTIVE",
      "choices": [
        {
          "id": "a",
          "text": "선택지 A"
        }
      ]
    }
  ]
}
```

### GET `/api/contents/recovery-mission-templates`

Public API.

Response data:

```json
{
  "templates": [
    {
      "conceptTag": "linear-function",
      "title": "일차함수 회복 미션",
      "prompt": "미션 설명",
      "hints": ["힌트 1"],
      "estimatedMinutes": 10,
      "status": "ACTIVE"
    }
  ]
}
```

### PUT `/api/contents/diagnostic-questions/{questionId}`

Requires Bearer token with `ADMIN` role.

Request:

```json
{
  "mathArea": "FUNCTION",
  "prompt": "문제 내용",
  "correctChoiceId": "a",
  "conceptTag": "linear-function",
  "displayOrder": 1,
  "status": "ACTIVE",
  "choices": [
    {
      "id": "a",
      "text": "선택지 A"
    },
    {
      "id": "unknown",
      "text": "잘 모르겠음"
    }
  ]
}
```

Validation:

- `choices`는 비어 있으면 안 된다.
- choice id는 중복되면 안 된다.
- `correctChoiceId`는 choices 안에 있어야 한다.
- `displayOrder`는 1 이상이어야 한다.
- `status`는 `ACTIVE` 또는 `INACTIVE`를 사용한다.

### PUT `/api/contents/recovery-mission-templates/{conceptTag}`

Requires Bearer token with `ADMIN` role.

Request:

```json
{
  "title": "일차함수 회복 미션",
  "prompt": "미션 설명",
  "hints": ["힌트 1", "힌트 2"],
  "estimatedMinutes": 10,
  "status": "ACTIVE"
}
```

Admin role 준비:

```sql
update user_accounts set role = 'ADMIN' where login_id = 'admin-login-id';
```

role 변경 후에는 다시 로그인해야 새 JWT에 role이 들어간다.

## 프론트 상태 설계 제안

최소 상태:

- `accessToken`
- `currentUser`
- `learningHome`
- `activeDiagnosticSession`
- `diagnosticQuestions`
- `selectedAnswers`
- `diagnosticResult`
- `activeMission`

라우팅 기준:

- access token 없음: login/signup
- access token 있음: learning home 호출
- `EMAIL_VERIFICATION_REQUIRED`: email verification screen
- `START_DIAGNOSTIC`: starting point screen
- `CREATE_RECOVERY_MISSION`: diagnostic result summary 또는 mission create CTA
- `CONTINUE_RECOVERY_MISSION`: mission detail

에러 처리 기준:

- `AUTH_UNAUTHORIZED` 또는 `GLOBAL_UNAUTHORIZED`: access token 제거 후 login으로 이동
- `AUTH_INVALID_REFRESH_TOKEN`: login으로 이동
- `DIAGNOSTIC_EMAIL_VERIFICATION_REQUIRED`: email verification screen으로 이동
- `DIAGNOSTIC_RESULT_NOT_READY`: questions 또는 submit flow로 이동
- `DIAGNOSTIC_ALREADY_COMPLETED`: result 조회로 이동
- `RECOVERY_MISSION_ALREADY_COMPLETED`: learning home refresh

## 화면 구현에서 지켜야 할 점

- 진단 문제는 session questions API 응답 순서를 그대로 렌더링한다.
- 진단 답변 제출 버튼은 모든 문항 선택 전까지 disabled 처리한다.
- 답변 제출 후 같은 세션에 재제출하지 않는다.
- 미션 제출 후에는 learning home을 다시 불러와 다음 행동을 갱신한다.
- `createdAt`, `updatedAt`, `completedAt`, `expiresAt`은 ISO-8601 string이다.
- enum 값은 서버 문자열을 그대로 저장하고, 화면 라벨만 프론트에서 매핑한다.
- 콘텐츠 관리 화면과 학생 화면의 API를 분리한다.

## 프론트 시작 순서

추천 구현 순서:

1. API client wrapper
2. Auth screens and token handling
3. Learning home screen
4. Math area selection and starting point
5. Diagnostic session/questions/answers
6. Diagnostic result
7. Recovery mission detail/submission
8. Admin content screen, if needed

이 순서가 좋은 이유는 `learning-home`이 전체 사용자 상태를 결정하므로, 먼저 붙이면 이후 화면 전환이 단순해진다.

## 백엔드와 추가로 맞추면 좋은 계약

프론트 개발 전에 백엔드에서 개선하면 좋은 항목:

1. dev 환경용 이메일 인증 토큰 확인 방식 제공
2. 학생에게 노출되지 않아야 하는 content read API를 운영 정책상 비공개로 돌릴지 결정
3. 관리자 화면에서 필요한 content validation 메시지 세분화

이미 반영된 항목:

- CORS 설정 추가
- local/dev refresh cookie secure 옵션 추가
- `/api/auth/login`, `/api/auth/me`에 `role` 추가

남은 항목 중 가장 먼저 볼 것은 이메일 인증 토큰 확인 방식이다. 이 부분이 없으면 로컬 회원가입 플로우 QA가 매번 서버 로그에 의존하게 된다.
