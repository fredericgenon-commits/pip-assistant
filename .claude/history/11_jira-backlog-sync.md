# 11 — JIRA backlog sync (Team Status + locked workloads)

## Prompts

### Session 1 (interview + plan)

Interview via the `/interview` skill:

> Récupération du backlog JIRA par REQ
>
> Pour chaque REQ du PIP, récupérer depuis JIRA les tickets DEV enfants (epic children)
> et calculer le backlog restant en story points par équipe, ainsi qu'un Team Status par
> équipe. [Full spec in prompt including TA detection, Team Status priority rules, ticket
> filtering, delivery-method, team-mapping, auto-sync design.]

Key decisions made during interview:
- Auto-sync (no button), silent, 10-min polling + interaction trigger (60 s threshold).
- **JIRA écrase la valeur manuelle** — when SP > 0 for (REQ, team), overwrite workload and
  lock it (`jira_locked=true`). When SP drops to 0, unlock automatically.

Plan approved via `ExitPlanMode`.

### Session 2 (continuation — context ran out)

Previous session ended mid-implementation. All backend JIRA files were missing (never
committed after the first session). This session re-created all files from scratch:

- Discovered via `Glob` that no `JiraProperties`, `JiraSyncService`, etc. existed.
- Domain service pattern: `BacklogCalculator` instantiated directly (`new BacklogCalculator()`)
  like `ImportDiffCalculator`, not a Spring bean.
- Fixed constructor arity mismatches in `PipDetailControllerTest` and `PipImportControllerTest`
  after adding `jiraLocked`/`teamStatuses` to `RequirementRow`.
- Fixed `Workload` 4→5 arg in `PipDetailServiceTest` after adding `jiraLocked` field.

## Result

**Backend (new files):**
- `JiraProperties` — `@ConfigurationProperties("pip.jira")` with field names + teamMapping
- `JiraPort`, `JiraBacklogPort` — domain ports
- `DevTicket` — domain record (key, summary, status, deliveryMethod, jiraTeam, storyPoints)
- `BacklogCalculator` — pure domain service, 20+ unit tests
- `JiraSyncResult`, `JiraSyncService` — application layer, 7 unit tests
- `MockJiraAdapter` (`@Profile("jira-mock")`) — deterministic: 2 DEV Core RFI 3SP + 1 TA Core Open + 1 DEV Portal TBE 5SP + 1 DEV Process Done
- `JiraClientAdapter` (`@Profile("!jira-mock")`) — real RestClient adapter
- `JiraSyncController` — `POST /api/pips/{id}/sync`, `GET /api/jira-sync-settings`
- `RequirementBacklogRepositoryAdapter` + JPA entity/repo
- `V6__jira_backlog.sql` — `jira_locked BOOLEAN` on workload + `requirement_backlog` table

**Backend (modified):**
- `Workload` domain record — added `jiraLocked` field
- `PipDetailRepository` port — added `upsertWorkloadFromJira`, `unlockWorkloadFromJira`, `updateRequirementStatus`
- `PipDetailRepositoryAdapter` — implemented new methods; manual Save never touches `jira_locked`
- `PipDetailView.RequirementRow` — added `jiraLocked` + `teamStatuses` maps
- `PipDetailService` — loads backlog repo + assembles jiraLocked/teamStatuses into rows
- `RequirementRowResponse` — added two new maps + `from()` mapping

**Frontend:**
- `pip-detail.model.ts` — `jiraLocked`, `teamStatuses` on `RequirementRow`; new interfaces `JiraSyncResult`, `JiraSyncSettings`
- `pip-detail.service.ts` — `syncJira()`, `getSyncSettings()`
- `pip-detail.ts` — auto-sync wiring (constructor + `@HostListener` + `interval`), `normalizeTeamStatus()`, `DatePipe`
- `pip-detail.html` — "Last synced" label, `teamStatus` column, `jiraLocked` disabling workload inputs, colspan 9→10
- `pip-detail.css` — `.sync-label`, `.team-status-badge` + 5 modifier classes

**Verified:**
- `mvnw test` — 68 tests, 0 failures
- `ng test --watch=false` — 16 tests, 0 failures
- Mock sync (PIP 2, 22 REQs): `synced=22, failed=0`
- API response includes `teamStatuses: {"1":"TA todo","2":"To be estimated","3":"Done"}` and `jiraLocked: {"1":true}`
