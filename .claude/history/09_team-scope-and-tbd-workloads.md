# Team scope filter + "TBD" workloads

## Prompt

« Renomme "Team comment" en "Team", on va élargir son utilisation. Ajoute une valeur
nulle/all. Lorsqu'aucune team n'est sélectionnée, les 3 champs "Requirements", "Projects" et
"Load/Capacité" affichent les données globales comme actuellement. Par contre lorsqu'une
équipe est sélectionnée, ces champs n'affichent que les projects, requirements et la capacité
qui impactent cette équipe (il y a une valeur ou un "TBD" dans la colonne de l'équipe).
L'utilisateur peut ajouter "TBD" (To be defined) dans les cellules des équipes. »

### Decision
- TBD persistence scope confirmed with the user: **full-stack** (survives reload).

## Result

**Frontend** — the PIP-detail header label becomes **Team**; the selector gains an **All**
option (nullable, default). Summary cards (`Requirements`, `Projects (TCM)`, `Load /
Capacity`) show global figures under `All`, or only what impacts the selected team otherwise
(`scopedRows()` / `impactsTeam()`; Load/Capacity switch to the team's own load and capacity).
Team workload cells became text inputs that accept **`TBD`** (`Number.parseFloat` drives
totals/sorting, `TBD`/empty → 0); the Dev-comment column shows `—` under `All`.

**Backend (TBD persisted)** — `workload.tbd` boolean (Flyway **V5**); `Workload` domain +
`WorkloadEntity` carry it (estimate null when TBD). The detail API now carries workloads as
**cell text** (`Map<Long, String>`) across `PipDetailView` / `RequirementRowResponse` and
`SavePipDetailRequest` / `SavePipDetailCommand` (capacities stay numeric). `PipDetailService`
parses each cell on save (`""`→clear, `TBD`→flag, else `BigDecimal`) and renders it on read;
`upsertWorkload(reqId, teamId, estimate, tbd)` marks a manual override when the number or the
TBD flag changes — the (numeric) Excel import never overwrites a TBD cell.

### Verified
- `./mvnw test` → 39 tests green (added `save_parsesTbdWorkloadCell`, TBD round-trip in
  `PipDetailRepositoryAdapterTest`; updated workload-as-string assertions across the detail
  service/controller/import controller tests).
- `npm run build` clean; `npx ng test --watch=false` → 16 tests green (added a team-scope
  filtering test; workloads-as-string stubs).

`doc/functional.md`, `doc/technical.md`, `CLAUDE.md` updated.
