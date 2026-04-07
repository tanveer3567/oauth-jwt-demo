# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a three-service OAuth2 + JWT authentication demo composed of:

- **`spring-auth/`** — Spring Boot auth service (port 8081): handles Google OAuth2 login/signup, mints a short-lived JWT (1h, HMAC-SHA256), and redirects to the Angular frontend with `?token=<jwt>`.
- **`resource-server/`** — Spring Boot REST service (port 8082): stateless, validates the JWT on every request via `JwtAuthFilter`, exposes `GET /api/hello`.
- **`web-client/`** — Angular 17 SPA (port 4200): initiates OAuth2 flows by navigating to `spring-auth`, captures the token from the callback URL, stores it in `localStorage`, and calls `resource-server` with it as a Bearer token.

Both backends share the same `JWT_SECRET` (env var or default dev value in `application.yml`). CORS is explicitly allowed from `http://localhost:4200` only.

## Running the Services

### Prerequisites
- Java 17, Maven
- Node.js + npm (for `web-client`)
- Google OAuth2 credentials (`GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`)

### spring-auth (port 8081)
```bash
cd spring-auth
GOOGLE_CLIENT_ID=<id> GOOGLE_CLIENT_SECRET=<secret> mvn spring-boot:run
```

### resource-server (port 8082)
```bash
cd resource-server
mvn spring-boot:run
```

### web-client (port 4200)
```bash
cd web-client
npm install
npm start
```

### Build (without running)
```bash
# Either backend:
mvn package -f spring-auth/pom.xml
mvn package -f resource-server/pom.xml

# Frontend:
cd web-client && npm run build
```

There are no automated tests in this project.

## Authentication Flow

1. User clicks Login/Signup → Angular navigates to `http://localhost:8081/login` or `/signup`.
2. `spring-auth` redirects to Google OAuth2. `/signup` adds `prompt=select_account` via `CustomAuthorizationRequestResolver`.
3. Google redirects back to `spring-auth`; `OAuth2AuthenticationSuccessHandler` mints a JWT and redirects to `http://localhost:4200/auth/callback?token=<jwt>`.
4. `AuthCallbackComponent` extracts the token from the query param, saves it to `localStorage`, and navigates to `/hello`.
5. `HelloComponent` calls `resource-server` at `http://localhost:8082/api/hello` with `Authorization: Bearer <token>`.
6. `JwtAuthFilter` in `resource-server` validates the token and populates `UserPrincipal` (email + name) into the security context.

## Key Configuration

| Setting | Location | Default |
|---|---|---|
| JWT secret | `app.jwt-secret` in both `application.yml` files | `my-very-secret-key-change-in-prod` |
| Frontend redirect after auth | `app.frontend-redirect` in `spring-auth/application.yml` | `http://localhost:4200/auth/callback` |
| Auth service URL | `web-client/src/app/auth/auth.service.ts` | `http://localhost:8081` |
| Hello API URL | `web-client/src/app/hello/hello.service.ts` | `http://localhost:8082` |

To change the JWT secret across all services, set the `JWT_SECRET` environment variable identically for both backends before starting them.
