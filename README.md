# PIP Assistant

Helps Scrum Masters prepare the backlog for the next **PIP** (a 7-week planning cycle:
6 weeks of development + 1 preparation week) by correlating the Project Managers' Excel
planning file with JIRA, GitLab and XLDeploy.

> Status: **Phase 1 — scaffold / hello world**. Empty hexagonal structure with an
> end-to-end `/api/health` round-trip. No business model or features yet.

## Repository layout

| Path | Description |
|------|-------------|
| `pip-assistant-backend/` | Spring Boot 4 API (Maven, Java 21, hexagonal architecture) |
| `pip-assistant-frontend/` | Angular 21 app (Tailwind CSS + Angular Material) |
| `doc/` | Functional & technical documentation |
| `.idea/runConfigurations/` | Shared IntelliJ run configurations |

## Prerequisites

- JDK 21+ (the backend targets Java 21; built locally with OpenJDK 22)
- Node.js 20+ and npm (validated with Node 24 / npm 11)
- Maven is not required — use the bundled `mvnw` wrapper

## Quick start

**Backend** (http://localhost:8080):
```bash
cd pip-assistant-backend
./mvnw spring-boot:run
# GET /api/health -> {"status":"UP"}
```

**Frontend** (http://localhost:4200, proxies `/api` to the backend):
```bash
cd pip-assistant-frontend
npm install
npm start
```

**IntelliJ IDEA**: open the repository root. Run `PIP Assistant (Full Stack)` to start
the backend and frontend together.

## Documentation

- [Functional documentation](doc/functional/README.md)
- [Technical documentation](doc/technical/README.md)
- [Project guidance for Claude Code](CLAUDE.md)
