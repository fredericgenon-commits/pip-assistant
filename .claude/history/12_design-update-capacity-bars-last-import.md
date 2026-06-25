# Design update — Capacity bars & Last import panel

## Prompt

« J'ai fait des modifications dans le frontend, je voudrais que tu le mette à jour. Voici
le prompt claude.design. N'oublie pas que l'application doit être en anglais.
Use the claude_design MCP (…) to import this project:
https://claude.ai/design/p/e0ece246-24c2-477f-b038-f606c58c6381?file=PIP+Assistant.dc.html
Implement: PIP Assistant.dc.html »

## Changes implemented

### Design diff analysis

The updated `PIP Assistant.dc.html` introduced three changes relative to the existing
Angular implementation:

1. **4th summary card "Capacity per team"** — mini progress bars (load/capacity) per team,
   displayed in a `grid: repeat(3, 1fr)` layout. Bars turn red when load > capacity.
2. **"Last import" info panel** next to the Excel drop zone — shows version badge (e.g. v4),
   original filename, and import date.
3. **Team selector label** renamed from "Team" to "Team comment" (the selector only affects
   which team's dev-comment column is visible, not the global scope).

### Backend changes

- `ExcelImportRepository` (port): added `findLastImportMeta(Long pipId)` returning
  `Optional<ImportMeta>` (versionNo, originalFilename, importedAt).
- `ExcelImportRepositoryAdapter`: implemented via `findFirstByPipIdOrderByVersionNoDesc`.
- `PipDetailView`: added `LastImport` inner record and `lastImport` nullable field.
- `PipDetailService`: injected `ExcelImportRepository`; `getDetail()` now fetches and
  includes last import metadata.
- `PipDetailResponse`: added `LastImportResponse` record and included in `from()`.
- Tests updated: `PipDetailServiceTest` (new mock + stub), `PipDetailControllerTest`
  and `PipImportControllerTest` (`null` passed for new field).

### Frontend changes

- `pip-detail.model.ts`: `LastImport` interface; `PipDetail.lastImport` field.
- `pip-detail.ts`: `lastImport` signal; `apply()` maps ISO string to `Date`;
  `teamCap()` and `teamPct()` helpers for progress bars.
- `pip-detail.html`:
  - 4th summary card `.cap-bars-card` with `@for` over teams.
  - `@if (lastImport())` panel next to the drop zone.
  - "Team comment" label on the team selector.
- `pip-detail.css`: `.cap-bars-card`, `.cap-bar*`, `.last-import*` rules; import zone
  changed to `display:flex` with the drop zone getting `flex:1 min-width:340px`.

## Result

- 68 backend tests pass (0 failures).
- Frontend compiles and serves correctly.
- Verified visually on 26_PIP_2: capacity bars show (red for over-capacity teams),
  "Last import v4 · 26_PIP_2.xlsx · 24 Jun 2026" panel visible, "Team comment" label correct.
- TC-13, TC-14, TC-15 added to test-cases.md.
