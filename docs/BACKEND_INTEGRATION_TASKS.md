# Backend Integration Tasks

Last updated: 2026-05-19

This is the remaining checklist before Naroo can be treated as beta-ready. The frontend already has API repository wiring for auth, learning home, diagnostics, and recovery missions. The next work is not more mock UI. The next work is proving the real backend flow with real content.

## Current Frontend State

- Flutter app structure is split by app, domain, data, and UI layers.
- Auth API repository is connected.
- Learning home, diagnostic, and recovery mission API repositories are connected.
- Flutter web support is enabled and `flutter build web` passes.
- Mock/unit/widget tests pass.
- Browser QA verified that the web app renders from a release build.
- Browser QA verified graceful failure when `http://localhost:8080` is offline.

## Backend Must-Do

### 1. Run the full backend stack locally

Required services:

- Spring backend on `http://localhost:8080`
- MySQL
- Redis
- Local email verification token path

Expected local command from the backend repo:

```bash
NAROO_AUTH_REFRESH_COOKIE_SECURE=false ./gradlew bootRun
```

Optional local QA command with a seeded verified student account:

```bash
NAROO_AUTH_REFRESH_COOKIE_SECURE=false \
NAROO_SEED_STUDENT_ENABLED=true \
./gradlew bootRun
```

The frontend cannot complete real login, diagnostic, or recovery QA until this stack is online.

### 2. Seed beta content

The app needs real beta content before student testing:

- Math areas
- Diagnostic questions
- Choices
- Correct choice IDs
- Concept tags
- Recovery mission templates
- Hints
- Estimated minutes

Minimum content target:

- 5 math areas
- 5-10 diagnostic questions for the first beta wedge
- 1 recovery mission template per primary weak link

Priority beta wedge:

- High-school math re-entry
- First focus: function graph / linear function recovery

### 3. Confirm API contracts with real responses

Frontend currently expects the contracts documented in `docs/frontend-integration-guide.md`.

Must verify these endpoints end-to-end:

```text
POST /api/auth/sign-up
POST /api/auth/login
POST /api/auth/email/verify
POST /api/auth/email/resend
POST /api/auth/reissue
GET  /api/auth/me
GET  /api/me/learning-home
GET  /api/math-areas
POST /api/diagnostics/starting-point
POST /api/diagnostics
GET  /api/diagnostics/{diagnosticSessionId}/questions
POST /api/diagnostics/{diagnosticSessionId}/answers
GET  /api/diagnostics/{diagnosticSessionId}/result
POST /api/recovery-missions
GET  /api/recovery-missions/{recoveryMissionId}
POST /api/recovery-missions/{recoveryMissionId}/submissions
```

The most fragile response fields are:

- `emailVerified`
- `role`
- `nextAction`
- `latestDiagnostic.diagnosticSessionId`
- `latestDiagnostic.weakLinks`
- `todayMission.id`
- `questions[].choices[].id`
- `primaryRecoveryConcept`
- `nextMissionPreview.title`

### 4. Provide local email verification workflow

Current assumption: local email sender logs the verification token.

For fast QA, backend should provide one of these:

- Clear log line with the token and email address
- Dev-only endpoint for retrieving latest verification token
- Seeded verified test account

Recommended for beta QA: seeded verified test account plus log-based verification for signup testing.

Seeded verified test account is available when `NAROO_SEED_STUDENT_ENABLED=true`:

```text
loginId: student01
email: student01@example.com
password: password123
nickname: 나루
mathStatus: UNKNOWN
```

For Railway production QA, do not leave the public default credentials enabled.
Use the same seed path, but override `NAROO_SEED_STUDENT_LOGIN_ID`, `NAROO_SEED_STUDENT_EMAIL`, and `NAROO_SEED_STUDENT_PASSWORD` with random values.

### 5. Verify refresh cookie behavior

The frontend stores access token in memory and expects refresh token as an HttpOnly cookie.

Backend must verify:

- Login sets `refresh_token` cookie.
- Cross-site web deployments use `SameSite=None; Secure` when frontend and backend are on different sites.
- `/api/auth/reissue` works with credentials included.
- Local HTTP uses `NAROO_AUTH_REFRESH_COOKIE_SECURE=false`.
- Production uses secure cookie settings.
- CORS allows the deployed frontend origin.

### 6. Prepare beta deployment environment

The frontend can now read API base URL from Dart define:

```bash
bash scripts/build-frontend-production.sh
```

Backend deployment must provide:

- Public API URL: `https://backend-production-688a6.up.railway.app` for the current Railway deployment
- CORS allowed origin for frontend URL: `https://naroo.app`
- MySQL production/staging connection
- Redis production/staging connection
- JWT secret
- Refresh cookie secure settings: `NAROO_AUTH_REFRESH_COOKIE_SECURE=true`
- Refresh cookie same-site settings: `NAROO_AUTH_REFRESH_COOKIE_SAME_SITE=None` while the backend stays on the Railway domain

## Frontend Already Possible Now

These items were handled in the frontend repo:

- Web platform support was added.
- Release web build was verified.
- API base URL can be configured with `NAROO_API_BASE_URL`.
- Backend-offline login error is shown gracefully.
- Deferred backend/API QA work is tracked in `TODOS.md`.

## End-to-End QA Script

Run this only after backend services are online.

### 1. Build or run frontend

Local web server:

```bash
flutter run -d web-server --web-port 3000 \
  --dart-define=NAROO_API_BASE_URL=http://localhost:8080
```

Release build:

```bash
flutter build web \
  --dart-define=NAROO_API_BASE_URL=http://localhost:8080
python3 -m http.server 3000 --directory build/web
```

### 2. Test student flow

Use a clean test account:

```text
loginId: student01
email: student01@example.com
password: password123
nickname: 나루
mathStatus: UNKNOWN
```

Flow:

1. Sign up.
2. Verify email.
3. Log in.
4. Confirm learning home loads.
5. Select starting point.
6. Create diagnostic session.
7. Load diagnostic questions.
8. Submit all answers, including at least one `unknown`.
9. Confirm weak-link result.
10. Create recovery mission.
11. Submit mission answer.
12. Return to learning home and confirm next action changed.

### 3. Expected pass criteria

- No blank screen.
- No console errors except renderer warnings in headless browser.
- Authenticated calls include bearer token.
- 401 triggers one refresh attempt.
- Student diagnostic questions do not expose correct answers.
- Result screen uses backend weak links, not mock scoring.
- Recovery mission content comes from backend template.
- Mission submission completes and updates home state.

## Not Done Yet

- Real backend E2E QA.
- Production/staging deployment QA.
- Accessibility QA for Flutter web semantics.
- Real beta student feedback loop.
