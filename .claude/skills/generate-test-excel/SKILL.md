---
name: generate-test-excel
description: Generate a coherent PIP planning .xlsx test fixture for the PIP Assistant project. Use when the user wants a sample/test Excel file mimicking the Project Managers' planning file (TCM, REQ, comment, per-team workloads), to test the PIP Details screen or the future Excel import. Can emit a single file or a v1 + v2 delta pair to exercise update tracking.
---

# Generate test Excel — PIP Assistant

Produces an `.xlsx` planning file matching what Project Managers send to development,
with business-coherent fake data (JIRA key prefixes, the six fixed teams, TCM→REQ
hierarchy, story points). No app or backend is required — this only writes a file.

## Columns (one row per REQ)

`TCM`, `TCM Description`, `REQ`, `REQ Description`, `Comment`, then one story-point
column per team in seeded order: `Core`, `Portal`, `Process`, `Assets`, `API`,
`Document`. Status and dev comments are **not** in the PM file (they are entered in the
app), so they are intentionally omitted.

Coherence rules enforced by the generator:
- TCM keys `TCM-<n>` group several REQ and repeat across their rows;
- REQ keys `REQ-<n>` are unique and sequential;
- each REQ has at least one non-empty team workload; workloads are sparse and plausible
  (Fibonacci points, occasional half points);
- descriptions are drawn from a life-insurance platform vocabulary.

## How to run

The generator is a self-contained Node script (uses `exceljs`). From this skill folder:

```bash
cd .claude/skills/generate-test-excel
npm install            # once, installs exceljs
node generate.js --versions 2 --seed 42
```

Output goes to `<repo>/test-data/` by default.

### Options

| Option | Default | Meaning |
|--------|---------|---------|
| `--pip <code>` | `26_PIP_1` | PIP code used in the file name |
| `--tcm <n>` | `5` | number of TCM projects |
| `--reqPerTcm <a-b>` | `1-4` | REQ count range per TCM |
| `--versions <1\|2>` | `1` | `1` = single file; `2` = v1 + v2 delta |
| `--seed <n>` | random | seed for reproducible output |
| `--out <dir>` | `<repo>/test-data` | output directory |

`node generate.js --help` prints the same reference.

### Output

- `--versions 1` → `<out>/<pip>.xlsx`
- `--versions 2` → `<out>/<pip>_v1.xlsx` and `<out>/<pip>_v2.xlsx`

The v2 file keeps the same keys as v1 but adds realistic deltas (re-estimated
workloads, updated comments, an added REQ, sometimes a new TCM or a dropped REQ) so the
future update-tracking feature can be exercised.

## When to use

Use when asked to "create a test Excel file", "generate sample PIP data", "produce a
planning fixture", or to seed the PIP Details screen / future Excel import with coherent
data. If the user needs data injected into the running app instead of a file, that is a
different task (the REST endpoints `POST /api/pips`, `POST /api/pips/{id}/requirements`,
`PUT /api/pips/{id}/detail`) — this skill only writes a file.
