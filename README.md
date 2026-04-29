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

Stop local infrastructure:

```bash
docker compose down
```
