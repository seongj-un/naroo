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

Stop local infrastructure:

```bash
docker compose down
```
