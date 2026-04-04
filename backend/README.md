# IDVE Backend (Spring Boot JWT)

## Stack
- Spring Boot 3
- Spring Security (stateless)
- JWT (jjwt)
- Spring Data JPA
- H2 (default), MySQL (optional)

## Run
1. Make sure Java 17+ is installed.
2. From `backend` folder, run:

```bash
mvn spring-boot:run
```

Backend starts on `http://localhost:8080`.

## Environment Variables
Create a local `.env` file in `backend` (already gitignored) and set:

- `APP_JWT_SECRET` (required, use a long random value)
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GITHUB_CLIENT_ID`
- `GITHUB_CLIENT_SECRET`
- `OAUTH2_REDIRECT_URI` (default: `http://localhost:5173/login`)

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
Default is in-memory H2 for quick testing.

To use MySQL, update datasource settings in `application.yml` using the commented example.
