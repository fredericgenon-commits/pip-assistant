# PIP list screen (first vertical slice)

## Prompt

« Pour le prochain développement, on va créer le premier écran : une page qui affiche les
PIPs (titre "PIPs"). Le nom d'un PIP est `yy_PIP_n` (ex. `26_PIP_1`). Écran : filtre
"Année", bouton "Refresh", bouton "New", liste des PIPs. Bouton New : fenêtre avec un
champ "PIP name" éditable, initialisé en partant du PIP maximum (`26_PIP_4` → `26_PIP_5`,
ou `yy_PIP_` si vide), boutons "Save" / "Cancel". » — revue puis cadrage via `/interview`.

### Decisions locked in the interview
- **next-code**: greatest existing code, increment only `n`; year not auto-incremented;
  empty DB → `<currentYY>_PIP_1`.
- **List**: columns *PIP name* + *Status* (badge), sorted descending; rows clickable →
  PIP detail (stub for now).
- **Status enum**: `PREPARATION` (default) / `ACTIVE` / `CLOSED`.
- Dates/status not edited here; endpoints open (Security deferred); year filter default "All".

## Result

First real feature delivered end-to-end (hexagonal reference pattern) + Angular router.

**Backend** (`com.utmost.lu.pipassistant`):
- domain `PipCode` (value object: validate `^\d{2}_PIP_\d+$`, year/sequence, ordering,
  next), `Pip`, `PipStatus`, port `PipRepository`.
- application `PipService` (+ injected `Clock`): `list`, `distinctYears`,
  `suggestNextCode`, `create`; `DuplicatePipCodeException`.
- infrastructure: `PipEntity` + `PipJpaRepository` + `PipRepositoryAdapter`;
  `PipController` + DTOs; `PipExceptionHandler` (409/400); `ClockConfig`.
- Flyway `V2__create_pip.sql` (table `pip`, unique `code`).
- Endpoints: `GET /api/pips?year=`, `GET /api/pips/years`, `GET /api/pips/next-code`,
  `POST /api/pips`.

**Frontend** (`src/app`):
- `App` becomes a shell (`mat-toolbar` + `router-outlet`); routes `'' → pips`,
  `pips` → `PipList`, `pips/:id` → `PipDetail` (stub). Health demo removed.
- `pips/`: `pip.model.ts`, `pip.service.ts`, `pip-list` (table + year filter + Refresh +
  New), `pip-new-dialog` (reactive form, prefilled next code, 409/400 handling),
  `pip-detail` (stub).

**Tests & verification**:
- Backend `./mvnw test` → 15/15 (service with fixed `Clock`, `@WebMvcTest` controller,
  `@DataJpaTest` adapter).
- Frontend `npm run build` OK (raised initial budget to 1MB after adding Material) and
  `ng test` → 8/8.
- Live e2e: next-code on empty = `26_PIP_1`; POST 201 then 409 (duplicate); 400 on bad
  format; `next-code` after `26_PIP_9`/`26_PIP_10` = `26_PIP_11` (numeric); list desc;
  `years` = `[26]`.

**Notes / residual**: detail page is a stub; `PipResponse` exposes `id, code, status`
(dates later); `MatDialog` runs without `@angular/animations` (no transition); V2 DDL is
H2-oriented (adapt identity for MS SQL Server later).
