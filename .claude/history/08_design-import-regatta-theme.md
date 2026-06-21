# Design import — Regatta theme (PIP list & detail re-skin)

## Prompt

« Make sure the claude_design MCP connector is connected … use the claude_design MCP tools
to import this project: https://claude.ai/design/p/e0ece246-… (file=PIP Assistant.dc.html).
Implement: PIP Assistant.dc.html »

The design project bundled a two-screen prototype (Liste des PIPs + Détails du PIP) in the
proprietary "Design Component" format, plus a `design_handoff_pip_assistant/README.md`
hand-off. Two visual directions were offered (Regatta / Marina); the hand-off recommends
**Regatta** and marks the **French UI copy as definitive**.

### Decisions
- Imported the design via the `DesignSync` MCP tool (`get_project` / `list_files` /
  `get_file`); treated the `.dc.html` as a visual reference, not code to copy.
- **Regatta** theme; **French** interface copy (code identifiers stay English).
- **Période** column shows *À planifier* — the backend `PipResponse` still omits dates and
  nothing captures them yet; no backend change made (kept scope to the frontend).
- Last-import metadata card from the mock omitted (no data source); the functional
  drag-&-drop import zone was restyled instead.

## Result

Frontend-only re-skin, no business logic moved to the client.

- **Theme** — Regatta tokens as `:root` CSS variables + shared classes in `styles.css`;
  Google Fonts in `index.html`.
- **App shell** — header bar (logo/wordmark/nav/avatar) + 4 px stripe replaces the
  `mat-toolbar` (`app.ts/html/css`); content centred at 1340 px.
- **PIP list** — French copy, title/subtitle, toolbar, card table with status badges +
  *Ouvrir ›*, *Période* (À planifier). New-PIP dialog rebuilt with a custom panel (mono
  input, hint, French validation) over `MatDialogRef`.
- **PIP detail** — breadcrumb, title + status badge, team select + *Enregistrer*
  (→ *Enregistré ✓*), three summary cards (with over-capacity red), restyled import zone,
  six-status diff legend, and the `mat-table` grid given a grouped super-header, per-status
  row colours, badge-styled REQ status `<select>`, and red Total/Capacity on over-capacity.

### Verified
- `npm run build` → bundle OK (raised the `anyComponentStyle` budget 4→8 kB for the grid).
- `npx ng test --watch=false` → 15 tests green (specs updated for the new markup: appbar,
  list table rows, French empty message, detail breadcrumb).

`doc/functional.md`, `doc/technical.md`, `CLAUDE.md` updated.
