# Naroo

## Local Development

Start PostgreSQL and Redis:

```bash
docker compose up -d
```

Run the application:

```bash
./gradlew bootRun
```

Default local services:

- PostgreSQL: `localhost:5432`, database `naroo`, user `naroo`, password `naroo`
- Redis: `localhost:6379`

Stop local infrastructure:

```bash
docker compose down
```
