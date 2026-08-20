# IDVE Backend (Spring Boot JWT)

## Stack
- Spring Boot 3.3.4
- Spring Security (stateless)
- JWT (jjwt)
- Spring Data JPA
- PostgreSQL (runtime), H2 (test only)

## Run
1. Make sure Java 17+ is installed.
2. From `backend` folder, run:

```bash
mvn spring-boot:run
```

Backend starts on `http://localhost:8080`.

## Environment Variables
Create a local `.env` file in `backend` (already gitignored) and set:

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (PostgreSQL connection)
- `APP_UPLOADS_PATH` (optional, defaults to `backend/uploads`)
- `APP_JWT_SECRET` (required, use a long random value)
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`
- `OAUTH2_REDIRECT_URI` (default: `http://localhost:5173/login`)
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`

## Auth APIs
- `POST /api/auth/register`
  - body: `{ "name": "...", "email": "...", "password": "..." }`
- `POST /api/auth/login`
  - body: `{ "email": "...", "password": "..." }`
  - response: `{ "token": "..." }`

## Protected API
- `GET /api/user/me`
- header: `Authorization: Bearer <token>`

## CORS
Allowed frontend origins are configured in `application.yml`:
- `http://localhost:3000`
- `http://localhost:5173`
- `http://localhost:5174`

## Database
PostgreSQL is the default and only runtime database.

Start a local PostgreSQL instance (e.g., via Docker):

```bash
docker run -d --name idve-pg -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=idve_db \
  -p 5432:5432 postgres:16-alpine
```

Then create a `.env` file in `backend/` with your credentials (see `.env.example`).
Tests use H2 in-memory (test-scoped) and run automatically with `mvn test`.
