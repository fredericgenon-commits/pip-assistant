# generate-test-excel

Standalone Node tool that generates a PIP Assistant **planning `.xlsx` test fixture** —
the kind of file Project Managers send to development, with business-coherent fake data.

This is a Claude Code project skill (see `SKILL.md`), but it also runs by hand.

## Install & run

```bash
cd .claude/skills/generate-test-excel
npm install
node generate.js --versions 2 --seed 42
```

Files are written to `<repo>/test-data/` by default.

## Columns

One row per REQ:

| TCM | TCM Description | REQ | REQ Description | Comment | Core | Portal | Process | Assets | API | Document |
|-----|-----------------|-----|-----------------|---------|------|--------|---------|--------|-----|----------|

The six team columns hold story points (sparse — a REQ only loads a subset of teams).
Status and dev comments are entered in the app, not in the PM file, so they are excluded.

## Options

| Option | Default | Meaning |
|--------|---------|---------|
| `--pip <code>` | `26_PIP_1` | PIP code used in the file name |
| `--tcm <n>` | `5` | number of TCM projects |
| `--reqPerTcm <a-b>` | `1-4` | REQ count range per TCM |
| `--versions <1\|2>` | `1` | `1` = single file; `2` = v1 + v2 delta |
| `--seed <n>` | random | seed for reproducible output |
| `--out <dir>` | `<repo>/test-data` | output directory |

A fixed `--seed` reproduces byte-for-byte the same data, which is handy for tests.

## Files

- `generate.js` — CLI entry point (arg parsing + exceljs workbook writing).
- `lib/data.js` — business data generation (keys, teams, sparse workloads, v2 delta).
- `package.json` — declares the single `exceljs` dependency.
