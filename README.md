# Naroo

## Local Development

Start MySQL and Redis:

```bash
docker compose up -d
```

Run the application:

```bash
./gradlew bootRun
```

Default local services:

- MySQL: `localhost:3307`, database `naroo`, user `naroo`, password `naroo`
- Redis: `localhost:6379`
- Email verification sender: local logging adapter. Check the application log for the verification token.

Useful auth endpoints:

- `POST /api/auth/sign-up`
- `POST /api/auth/email/verify`
- `POST /api/auth/login`
- `POST /api/auth/reissue`
- `GET /api/auth/me`
- `POST /api/diagnostics/starting-point`

Example signup body:

```json
{
  "loginId": "student01",
  "email": "student01@example.com",
  "password": "password123",
  "nickname": "나루",
  "mathStatus": "UNKNOWN"
}
```

Example email verification body:

```json
{
  "token": "token-from-application-log"
}
```

Example starting point body:

```json
{
  "selectionType": "WEAK_AREA",
  "mathArea": "FUNCTION",
  "note": "함수가 제일 헷갈려요"
}
```

Stop local infrastructure:

```bash
docker compose down
```
