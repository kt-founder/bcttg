# Deploy Guide

This app is packaged for one-step deployment using Docker Compose.

## Prerequisites
- Docker 24+
- Docker Compose v2

## One-step run (Windows/Linux)
```bash
docker compose up --build
```

Services:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui
- API Docs: http://localhost:8080/api-docs
- MySQL: localhost:3306 (user/pass: bcttg / bcttg)

## DB setup (automatic)
No manual DB setup is required. The MySQL container is created by Docker Compose and Flyway runs migrations + seed data automatically on app startup.

## Environment variables (docker-compose)
You can edit `docker-compose.yml` or export env vars before running:

- `BCTTG_JWT_SECRET` (required, min 32 chars recommended)
- `BCTTG_MEDIA_ROOT` (default in container: `/data/media`)
- `BCTTG_MEDIA_BASE_URL` (default: `http://localhost:8080`)
- `SPRING_PROFILES_ACTIVE` (default: `docker`)

## Default users (dev)
Configured in `src/main/resources/application.yml`:
- admin / admin123
- manager / manager123
- user / user123

Change these in production.

## Data & persistence
Docker volumes:
- `mysql_data`: MySQL data
- `media_data`: uploaded files

To reset local data:
```bash
docker compose down -v
```

## Database migrations & seed
Flyway runs automatically on startup.
- Schema: `src/main/resources/db/migration/V1__init.sql`
- Seed/mock data: `src/main/resources/db/migration/V2__seed.sql`

## Production notes
- **Rotate JWT secret** and use strong credentials.
- Restrict `/files/**` access if required by your security policy.
- Use HTTPS and a reverse proxy (Nginx/Traefik) if exposed publicly.
- Back up the MySQL volume regularly.

### Nginx upload limit (50MB)
If your API is behind Nginx, configure body size to avoid `413 Request Entity Too Large`.

- Sample config file: `deploy/nginx/bcttg.conf`
- Includes `client_max_body_size 50m;` for `api.hotrocode.tech` and `bcttg.io.vn`

Apply on server:
```bash
sudo cp deploy/nginx/bcttg.conf /etc/nginx/sites-available/bcttg.conf
sudo ln -sf /etc/nginx/sites-available/bcttg.conf /etc/nginx/sites-enabled/bcttg.conf
sudo nginx -t
sudo systemctl reload nginx
```

## Troubleshooting
- API can?t connect to DB: wait for MySQL to be healthy, or check compose logs.
- Flyway errors: ensure DB is empty or compatible with the current migrations.
