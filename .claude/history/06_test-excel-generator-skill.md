# Test Excel generator skill

## Prompt

« Crée une skill pour ce projet qui permet de tester le pip-assistant en créant un fichier
xls de test. Sers-toi des informations qui sont dans le projet pour définir les colonnes
nécessaires. La skill doit être capable de créer des données cohérentes par rapport au
business case de l'application. » — cadré via `/interview`.

### Decisions locked in the interview
- **Runtime**: Node + **exceljs** (reuses the Node toolchain already present; no Python
  dependency). The script is self-contained with its own `package.json`, isolated from the
  Angular frontend.
- **Scope**: generate the **`.xlsx` file only** — no REST calls, no DB seeding (the Excel
  import does not exist yet; the file is a fixture for the future parser and for eyeballing
  the PIP Details screen).
- **Versions**: **multi-version** — can emit a v1 and a derived **v2 delta** (stable keys)
  to exercise the future update-tracking feature.

### Columns (derived from the project)
One row per REQ, from `doc/functional.md` (§ "Each row contains") and
`V3__create_pip_detail.sql`: `TCM`, `TCM Description`, `REQ`, `REQ Description`, `Comment`,
then one story-point column per seeded team (`Core`, `Portal`, `Process`, `Assets`, `API`,
`Document`). Status and dev comments are app-side, so they are excluded from the PM file.

## Result

Created the project skill `.claude/skills/generate-test-excel/`:
- `SKILL.md` — trigger + run instructions; `README.md` — standalone usage.
- `package.json` — single `exceljs` dependency.
- `generate.js` — CLI (`--pip --tcm --reqPerTcm --versions --seed --out`, plus `--help`).
- `lib/data.js` — business data generation: seeded PRNG (reproducible), `TCM-`/`REQ-`
  sequential keys, TCM→REQ grouping, sparse per-team Fibonacci workloads (each REQ has ≥1
  non-empty team), themed insurance-platform descriptions, and `deriveV2` (re-estimated
  workloads, updated comments, added REQ, sometimes a new TCM or a dropped REQ).

Defaults write to `<repo>/test-data/` (git-ignored).

### Verified
- `npm install` (exceljs) then `node generate.js --versions 2 --seed 42` produced
  `test-data/26_PIP_1_v1.xlsx` and `_v2.xlsx` (5 TCM / 9 REQ).
- Re-read both files with exceljs: 11 columns in the expected order, bold frozen header,
  TCM keys repeated across their REQ, unique sequential REQ keys, sparse workloads with no
  empty REQ rows, and identifiable v1→v2 deltas (REQ-530 added, REQ-528 dropped, several
  workloads re-estimated, a comment updated) while shared keys stay stable.
- `--help` prints the option reference.

`.gitignore`: added `test-data/` (generated fixtures, regenerate on demand).
