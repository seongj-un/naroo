# Naroo Beta Deployment Strategy

This document defines the minimum deployment strategy for a private Naroo beta.

## Goal

Naroo beta needs a private web link that 5-10 target students can open from their phones.

The deployment should support:

- Spring Boot/Kotlin backend
- Social login
- MySQL
- HTTPS
- Environment variables
- Private beta access

## Deployment Shape

Use a simple single-service deployment:

```text
Student browser
  -> HTTPS web app at https://naroo.app
  -> HTTPS API at https://api.naroo.app
  -> Spring Boot app
  -> MySQL
  -> OAuth provider
```

For beta, avoid Kubernetes, multi-service architecture, queues, or custom infrastructure.

## Production Domains

Use separate frontend and API origins for beta:

- Frontend: `https://naroo.app`
- Backend API: `https://api.naroo.app`

Do not hard-code these URLs in source code. The frontend must receive the API URL at build time, and the backend must receive the allowed frontend origin through environment variables.

Production frontend build:

```bash
bash scripts/build-frontend-production.sh
```

Production backend CORS/cookie settings:

```text
NAROO_AUTH_REFRESH_COOKIE_SECURE=true
NAROO_CORS_ALLOWED_ORIGINS=https://naroo.app
```

Use [.env.production.example](../.env.production.example) as the deployment environment checklist.

## Runtime Target

The hosted service must support:

- Java 17
- Gradle build
- Spring Boot application startup
- HTTPS endpoint
- Environment variables
- Health check endpoint

Recommended app command pattern:

```text
./gradlew bootJar
java -jar build/libs/naroo-*.jar
```

The exact platform can be chosen later as long as it supports the runtime target above.

## Database Target

Use managed MySQL for beta.

Required settings:

- One production database
- One local/dev database
- Connection string provided through environment variables
- SSL enabled in hosted environment
- Daily backups if the provider supports them

Do not use in-memory storage for external beta, because next-day return and saved progress are success criteria.

## Required Environment Variables

```text
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=<mysql connection string>
OAUTH_PROVIDER=<google|kakao>
OAUTH_CLIENT_ID=<provider client id>
OAUTH_CLIENT_SECRET=<provider client secret>
APP_BASE_URL=https://<private-beta-domain>
SESSION_SECRET=<random secret>
```

If the selected OAuth provider needs provider-specific variables, keep them prefixed:

```text
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
KAKAO_CLIENT_ID=...
KAKAO_CLIENT_SECRET=...
```

## OAuth Redirect URLs

For local development:

```text
http://localhost:8080/login/oauth2/code/<provider>
```

For private beta:

```text
https://<private-beta-domain>/login/oauth2/code/<provider>
```

The OAuth app configuration must include both local and beta redirect URLs before external testing.

## Profiles

Use at least two Spring profiles:

```text
local
prod
```

Local profile:

- Local MySQL or test container
- Local OAuth redirect URL
- Debug-friendly logging

Prod profile:

- Hosted MySQL
- HTTPS base URL
- Secure cookies
- No debug logs with student data

## Secrets Rules

- Never commit OAuth secrets.
- Never commit database URLs.
- Never print access tokens in logs.
- Never log raw OAuth profile payloads.
- Never store student real names unless the product needs them.

## Beta Access

For the first beta, keep access private:

- Share the link manually.
- Do not index the app publicly.
- Do not add public marketing pages yet.
- Keep the first page task-focused: start diagnostic or continue progress.

## Health Check

Add a simple health endpoint before deployment:

```text
GET /actuator/health
```

It should verify app startup. Database readiness can be added once persistence is implemented.

## Release Checklist

Before sharing the link:

- App starts with `SPRING_PROFILES_ACTIVE=prod`.
- Database connection works.
- OAuth login works on the hosted URL.
- New social login creates one student account.
- Returning social login reuses the same account.
- Diagnostic attempt persists.
- Result can be reloaded after closing the browser.
- Behavior events are recorded for diagnostic start, diagnostic completion, result view, recovery start, hint request, unknown answer, recovery completion, next mission preview, and next-day return.
- Mobile 375px layout works.
- Error states do not expose stack traces.

## Staged Rollout

Do not send the first beta link to all testers at once.

Use this rollout:

```text
1. Founder self-test
   - Complete login
   - Complete diagnostic
   - View result
   - Start recovery mission
   - Complete recovery mission
   - Return after closing browser

2. One trusted student
   - Watch for OAuth, mobile layout, and confusing copy issues
   - Fix obvious blockers before inviting more people

3. Three students
   - Confirm diagnostic completion and recovery start events are recorded
   - Check whether anyone drops between result and recovery mission

4. Ten students
   - Run the full private beta only after the first three steps pass
```

Rollback rule:

If login, diagnostic completion, result view, or recovery start breaks for any tester, pause invitations and fix before adding more students.

## Beta Observability

Record behavior events for the first beta. These events are product learning data, not analytics vanity metrics.

Required events:

```text
diagnostic_started
diagnostic_completed
result_viewed
recovery_started
hint_requested
unknown_selected
recovery_completed
next_mission_preview_viewed
next_day_returned
```

Each event should include:

```text
student_id
attempt_id
question_set_version
mission_id
created_at
```

Do not include email, access tokens, raw OAuth profile payloads, or free-form student notes in behavior events.

Minimum beta dashboard questions:

- How many students started the diagnostic?
- How many completed it?
- How many viewed the result?
- How many started the first recovery mission?
- How many completed the first recovery mission?
- How many returned the next day?

## Not In Scope For Beta Deployment

- Kubernetes
- Multi-region deployment
- CDN tuning
- Automated blue/green deployment
- Public landing page
- Payment system
- School/teacher admin portal

## Open Decisions

- OAuth provider: Google, Kakao, or both.
- Hosting provider.
- MySQL provider.
- Private beta domain or temporary provider URL.

Make these decisions before inviting external testers.
