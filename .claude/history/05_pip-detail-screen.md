# PIP Details screen

## Prompt

« Crée un nouvel écran 'PIP Details' (titre "PIP Details"), ouvert au double-clic sur un
PIP de la liste. Champs : Nom, Status, et un tableau des requirements (TCM, TCM
description, REQ, REQ description, REQ status, PM comment, Dev comment [selon Team], et
Core/Portal/Process/Assets/API/Document en décimal). Un sélecteur Team pilote la colonne
Dev comment. Sous le tableau : Total (somme par équipe) et Capacity (éditable par équipe).
Tous les champs éditables sauf TCM et REQ. On peut trier sur chaque colonne. » — cadré
via `/interview`.

### Decisions locked in the interview
- **Requirements source**: edit-only, **no seed**, **no Add/Delete** in the UI (they will
  come from the future Excel/JIRA import). A backend create endpoint exists for tests/import.
- **Save**: single global **Save** → one bulk **PUT** persisting all edits.
- **REQ status**: String validated against a **configurable list in `application.yml`**
  (`pip.requirement.statuses`, default TODO/IN_PROGRESS/DONE), served by
  `GET /api/requirement-statuses`.
- Columns are **sortable** (the user clarified: sort per column, not filter).
- Opened by **double-click**; PIP name + status read-only.

## Result

Introduced the domain: `Team`, `Project`, `Requirement`, `Workload`, `DevComment`,
`PipCapacity` (Flyway `V3__create_pip_detail.sql`, seeding the 6 teams).

**Backend** (`com.utmost.lu.pipassistant`):
- Domain records + ports `PipDetailRepository`, `TeamRepository`; `RequirementStatusCatalog`
  port ⇐ `RequirementStatusProperties` (`@ConfigurationProperties`).
- `PipDetailService` (getDetail aggregate, bulk save with status validation, statuses,
  interim createRequirement); `PipNotFoundException` (404), `InvalidRequirementStatusException` (400).
- Per-table JPA entities + Spring Data repos + `PipDetailRepositoryAdapter`,
  `TeamRepositoryAdapter`; `PipDetailController` (GET/PUT detail, statuses, interim POST
  requirement) + DTOs + `PipDetailExceptionHandler`. `application.yml` gains the status list.

**Frontend** (`src/app/pips`):
- List rows now open on **double-click**.
- `pip-detail/`: model + service + component — editable `mat-table` (plain inputs/select
  cells), `MatSort` on every column, Team `mat-select` driving the Dev comment column, two
  `mat-footer-row`s (Total computed live, Capacity editable), dirty tracking + single Save (PUT).

**Tests & verification**:
- Backend `./mvnw test` → 26/26 (service with Mockito, `@WebMvcTest` controller,
  `@DataJpaTest` adapter).
- Frontend `npm run build` OK + `ng test` → 13/13.
- Live e2e: create PIP, interim-create a requirement, GET detail (6 teams + row + empty
  capacities), PUT edits (workloads 3/5, status DONE, dev comment, capacities 20/15) → 204,
  re-GET shows everything persisted; bad status → 400; unknown PIP → 404.

**Notes / residual**: no add/remove requirement in the UI; editing TCM description updates
the shared Project (last-write-wins); story points are `BigDecimal` (plain number inputs);
V3 DDL is H2-oriented (adapt for MS SQL Server later). Excel/JIRA import and a status-admin
screen remain future work.
