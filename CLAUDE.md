# CLAUDE.md — PIP Assistant

Project guidance for Claude Code. Keep this file up to date as the project evolves.

## What this project is

PIP Assistant helps Scrum Masters prepare the backlog for the next **PIP**
(a 7-week cycle: 6 weeks of development across 3 sprints + 1 preparation week).
Project Managers send a regularly-updated Excel file listing the next PIP's projects
(TCM, REQ, comment, and known workload per team: Core, Portal, Process, Assets, API,
Document). The app will:

- identify requirements and their development tickets in JIRA;
- track successive updates of the Excel file;
- manage comments and required actions per team and per REQ;
- correlate data across JIRA, GitLab and XLDeploy.

## Monorepo layout

```
pip-assistant/
├─ pip-assistant-backend/    # Spring Boot 4 (Maven, Java 21), hexagonal architecture
├─ pip-assistant-frontend/   # Angular 21 (Tailwind CSS + Angular Material)
├─ doc/                      # functional.md + technical.md (Markdown, Mermaid diagrams)
├─ .claude/history/          # prompt history (see "Prompt history" below)
└─ .idea/runConfigurations/  # shared IntelliJ run configs (versioned)
```

## Stack

**Backend** — Spring Boot 4.1.0, Java 21 (Amazon Corretto 21,
`C:\Program Files\Amazon Corretto\jdk21.0.11_10`), Maven via the `mvnw` wrapper. Hexagonal architecture: `domain`, `application`,
`infrastructure` under `com.utmost.lu.pipassistant`. Persistence: Spring Data JPA +
H2 (dev), schema managed by **Flyway** (`src/main/resources/db/migration`); the target
DB is MS SQL Server, so keep SQL portable. OpenAPI/Actuator available.
Spring Security + JWT will be added in a later phase.

**Frontend** — Angular 21 (standalone components, signals), Tailwind CSS 4 +
Angular Material. Dev server proxies `/api/**` to the backend (`src/proxy.conf.json`).

**Integrations (later phases)** — GitLab REST API v4, JIRA REST API, XLDeploy REST API.
Config (URLs + tokens) via `application.yml` / environment variables, never committed.

## Build, run & test

Backend (JAVA_HOME = Amazon Corretto 21, `C:\Program Files\Amazon Corretto\jdk21.0.11_10`):
```
cd pip-assistant-backend
./mvnw test            # run unit tests
./mvnw spring-boot:run # start API on http://localhost:8080  (GET /api/health -> {"status":"UP"})
```

Frontend:
```
cd pip-assistant-frontend
npm install
npm start              # ng serve on http://localhost:4200 (proxies /api to :8080)
npm run build
npx ng test --watch=false
```

IntelliJ: open the repo root. Three shared run configs are provided —
`Backend (Spring Boot)`, `Frontend (npm start)`, and the compound
`PIP Assistant (Full Stack)` that launches both.

## Conventions (must follow)

- **Language**: all code and documentation in **English**; communicate with the user in **French**.
- **Docs**: update `doc/functional.md` and `doc/technical.md` after every change.
  Use **Mermaid** diagrams where they clarify a model, flow or architecture.
- **Tests**: write JUnit tests (backend) / unit tests (frontend); run and verify the
  application after every change.
- Use **Context7** to confirm exact versions and API syntax (Spring Boot 4, Spring
  Security, Angular, Angular Material, Flyway, springdoc) before using a library.

## Domain model (target — for later phases)

Business entities (entered via the app): `Pip`, `Team`, `Project`, `Requirement`,
`Workload` (story points per team), `PipCapacity`, `Comment`, `Action`, `ExcelImport`.
Synced from external systems: `Ticket`, `Developer`, `Version`, `Release`.

Jira ticket categories drive the key prefix (e.g. `DEV-512`, `MNT-5155`, `TCM-120`):
DEV (development), MNT (maintenance), INV (investigation), REQ (requirement, parent = TCM),
TCM (project), REL (release, fix version `yyyy-nnn`). See `doc/functional.md` for full rules.

## Prompt history

Significant prompts and their outcomes are recorded under `.claude/history/`, **grouped
by feature/phase** (not by day — a feature often spans several days, and a single day may
touch several features). Files are numbered: `NN_<feature-slug>.md` (e.g.
`01_initial_prompt.md`, `02_environment-and-github.md`).

Each file has the same shape: a short title, the **prompt(s)** verbatim (or summarized
when long), and a `## Result` section describing what was delivered and verified. When a
new prompt clearly continues an existing feature, append to that file; otherwise start the
next number. Keep this history up to date as part of finishing a task.

## Current status

- Phase 1 (scaffold / hello world): complete — hexagonal structure + `/api/health`,
  Corretto 21, on GitHub.
- **PIP list** (`/pips`): complete. `Pip` entity end-to-end (domain `PipCode` value
  object, port + adapter, `PipService`, REST `/api/pips`, Flyway `V2`), Angular router
  shell + list/new-dialog.
- **PIP Details** (`/pips/:id`): complete. Domain `Team`/`Project`/`Requirement`/
  `Workload`/`DevComment`/`PipCapacity` (Flyway `V3`, seeds 6 teams), aggregated
  `GET/PUT /api/pips/{id}/detail`, configurable requirement statuses
  (`pip.requirement.statuses`). Editable worksheet (sortable grid, Total/Capacity footers,
  bulk Save). Requirements are not created in the UI yet — they await the Excel/JIRA import.
  Future: status/date editing, the import, and the integrations.
