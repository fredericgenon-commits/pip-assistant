# Technical Documentation — PIP Assistant

> Keep this document updated after every technical change.

## Architecture overview

Monorepo with two independently buildable projects:

- **Backend** (`pip-assistant-backend/`) — Spring Boot 4.1.0, Java 21, Maven.
  **Hexagonal architecture** (ports & adapters):
  - `domain` — entities, value objects, domain services, ports (empty in Phase 1).
  - `application` — use cases / application services (empty in Phase 1).
  - `infrastructure` — adapters: `web` (REST controllers), `config` (cross-cutting config).
- **Frontend** (`pip-assistant-frontend/`) — Angular 21 (standalone components, signals),
  Tailwind CSS 4, Angular Material. Talks to the backend over `/api`, proxied in dev.

## Backend

- **Build/runtime**: `mvnw` wrapper; targets Java 21 (`<java.version>21</java.version>`),
  compiled/tested locally with OpenJDK 22.
- **Persistence**: Spring Data JPA. Dev DB is H2 in-memory; schema owned by **Flyway**
  (`src/main/resources/db/migration`, starting at `V1__init.sql`). Hibernate
  `ddl-auto=none`. Target DB: **MS SQL Server** — keep migrations and types portable.
- **Configuration**: `src/main/resources/application.yml` (datasource, JPA, Flyway,
  Actuator). Secrets/URLs for integrations will come from env vars / non-committed files.
- **Endpoints (Phase 1)**: `GET /api/health` → `{"status":"UP"}` (plus Actuator at
  `/actuator/health`).
- **Tests**: JUnit 5. `PipAssistantBackendApplicationTests` (context loads),
  `HealthControllerTest` (`@WebMvcTest`). Note Spring Boot 4 moved test-slice annotations,
  e.g. `@WebMvcTest` is now in `org.springframework.boot.webmvc.test.autoconfigure`.

## Frontend

- **Tooling**: Angular CLI 21, `@angular/build` (esbuild). Tailwind 4 via PostCSS
  (`.postcssrc.json`, `@import 'tailwindcss'` in `src/styles.css`). Material theme in
  `src/material-theme.scss`.
- **HTTP**: `provideHttpClient(withFetch())` in `app.config.ts`; `HealthService` calls
  `/api/health`.
- **Dev proxy**: `src/proxy.conf.json` maps `/api/**` → `http://localhost:8080`,
  referenced from `angular.json` (`serve.options.proxyConfig`).
- **Tests**: Vitest via the `@angular/build:unit-test` builder (`npx ng test --watch=false`).

## Local development

| Action | Command |
|--------|---------|
| Backend tests | `cd pip-assistant-backend && ./mvnw test` |
| Backend run | `cd pip-assistant-backend && ./mvnw spring-boot:run` |
| Frontend install | `cd pip-assistant-frontend && npm install` |
| Frontend run | `cd pip-assistant-frontend && npm start` |
| Frontend build | `cd pip-assistant-frontend && npm run build` |
| Frontend tests | `cd pip-assistant-frontend && npx ng test --watch=false` |

CORS: the backend allows `http://localhost:4200` (`WebCorsConfig`) for direct calls;
the dev proxy is the primary mechanism during development.

## Planned (later phases)

- Spring Security + JWT, CORS hardening.
- Domain model + Flyway migrations (`V2__*`, ...) and full CRUD REST API + OpenAPI.
- Integrations: GitLab REST API v4, JIRA REST API, XLDeploy REST API.
- Excel import & change tracking.
