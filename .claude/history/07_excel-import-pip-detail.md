# Excel import on the PIP Detail screen (Slice 1)

## Prompt

« Fonctionnalité : Import Excel sur l'écran PIP Detail » — drag & drop d'un `.xlsx`,
import versionné par PIP, détection des lignes REQ, colonnes Priority + PIP status, diff
vs la version précédente, charges éditables mémorisées. Cadré via `/interview`.

### Decisions locked in the interview
- **Scope** = Slice 1 (import + diff). History viewer + rollback deferred to Slice 2.
- **No business logic in the frontend** (added as a rule in `CLAUDE.md`); all parsing /
  diff / priority / status / validation in the backend.
- **Parser** = Apache POI (`poi-ooxml`). **Columns matched by position** (`pip.import.*`),
  since file header labels may differ.
- **REQ detection** anchored on `REQ-xxx`; missing TCM key / TCM desc / REQ desc →
  `MISSING_DATA` (pale red). Non-REQ lines & extra columns ignored.
- **PIP status** (6, system): NEW, UNCHANGED, CHANGED, PRIORITY_CHANGED, REMOVED_FROM_PIP,
  MISSING_DATA. Diff identity = `REQ-xxx`, compared **file vs previous snapshot**.
- **Priority** recomputed each import (file order); removed REQ → null, shown at bottom,
  excluded from totals; default sort.
- **Manual override = workloads only**: an edited workload is not overwritten by re-imports.
- **Versioning per PIP**; each import stores a snapshot.

## Result

End-to-end Slice 1 delivered.

**Backend** — Flyway `V4` (requirement += priority/pip_status, workload += manual_override,
drop global `uq_requirement_key`, tables `excel_import`/`imported_requirement`/
`imported_workload`); domain `RequirementPipStatus` + parsed/snapshot/diff records + ports +
pure `ImportDiffCalculator`; `ImportExcelService`; `PoiExcelRequirementParser`;
`ExcelImportRepositoryAdapter` (snapshot + override-aware live upsert); `PipImportController`
`POST /api/pips/{id}/imports` + handler (404/422); `ExcelImportProperties`
(`pip.import.*` in `application.yml`). `PipDetailService.getDetail` sorts by priority
(removed last); manual-override set on the Save path.

**Frontend** (presentation only) — drop zone + "Import this file" button + `MatSnackBar`
toast; `import()` multipart; Priority (first) and PIP status columns; row colouring
(grey = removed, pale red = missing data); totals exclude removed rows; default sort priority.

### Verified
- `./mvnw test` → 38 tests green, incl. `ImportDiffCalculatorTest` (6 statuses + priority +
  removed), `PoiExcelRequirementParserTest` (skips non-REQ, flags missing data, rejects
  non-xlsx), `PipImportControllerTest` (200/422/404), `ExcelImportRepositoryAdapterTest`
  (version snapshot, **manual override preserved across re-import**, removed marking).
- `npx ng test --watch=false` → 15 tests green (dropzone accept/reject + import call).
- `npm run build` OK.
- Manual e2e: generated v1/v2 with `generate-test-excel`, imported both via the API and
  observed Priority, NEW on v1, then CHANGED/PRIORITY_CHANGED/UNCHANGED/REMOVED_FROM_PIP on v2.

`doc/functional.md`, `doc/technical.md`, `CLAUDE.md` updated.
