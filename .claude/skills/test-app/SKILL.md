---
name: test-app
description: >-
  Tests the PIP Assistant application end-to-end as a real user would, using
  browser automation via Playwright MCP. ALWAYS use this skill:
  (1) before any git push — fix ALL failing tests first, then push;
  (2) when the user says "teste", "vérifie", "valide l'appli", or similar;
  (3) proactively after fixing a bug or implementing a feature, to confirm
  nothing regressed.
  Reads test cases from test-cases.md in this directory (updated per feature);
  reads doc/functional.md for business context. Uses generate-test-excel for
  any test that needs an Excel fixture.
---

# test-app — End-to-end testing for PIP Assistant

Tests the application exactly as a user would: open a browser, navigate the
UI, interact with controls, verify what is displayed. The test cases are in
`test-cases.md` (same directory); the skill itself never changes — only that
file is updated when features are added or modified.

## Prerequisites

**Playwright MCP** must be configured (see "Setup" below). The backend and
frontend must be reachable at `http://localhost:8080` and
`http://localhost:4200`.

### Setup (first time only)

If `mcp__playwright__*` tools are NOT available in your context, the Playwright
MCP is not configured. Add it to `.claude/settings.json` at the repo root:

```json
{
  "mcpServers": {
    "playwright": {
      "command": "npx",
      "args": ["@playwright/mcp@latest", "--browser", "chromium", "--headless"]
    }
  }
}
```

Then restart Claude Code.

---

## Execution flow

### Step 1 — Ensure the app is running

Check `http://localhost:8080/api/health`. If it does not return `{"status":"UP"}`,
call the `start-app` skill (restart mode). Wait until health passes before
continuing.

### Step 2 — Read the test cases

Read `test-cases.md` from this skill directory
(`C:\workspace\pip-assistant\.claude\skills\test-app\test-cases.md`).

Also read `doc/functional.md` at the repo root for business context — it helps
interpret expected results and distinguish a bug from expected behaviour.

### Step 3 — Execute each test case

For each test case in `test-cases.md`:

1. Follow the **Steps** exactly, using Playwright MCP tools:
   - `browser_navigate` to open URLs
   - `browser_snapshot` to read page state (accessibility tree — prefer this
     over screenshots for verification)
   - `browser_click` / `browser_type` / `browser_select_option` to interact
   - `browser_screenshot` only when you need to visually confirm something
     complex (e.g. theme colours)
   - `browser_wait_for_timeout` sparingly, only when the UI needs time to load

2. Verify the **Expected results** against what `browser_snapshot` returns.

3. Mark each test case **PASS** or **FAIL**.
   - FAIL = expected result not observed, or an error/exception appeared.
   - If a step throws, note it and move to the next test case (don't abort the
     whole run).

4. For tests that need an Excel fixture, call the `generate-test-excel` skill
   to create one, then use `browser_file_chooser` or drag-and-drop simulation.

### Step 4 — Report results

After all test cases, produce a clear summary in this format:

```
## Test results — <date> <time>

| # | Test | Result | Note |
|---|------|--------|------|
| TC-01 | PIP List — page load | ✅ PASS | |
| TC-02 | PIP List — create PIP | ✅ PASS | |
| TC-03 | ...                   | ❌ FAIL | Dialog does not close after Save |

**Passed: 8 / 10**
```

List each failure with a concrete description of what was observed vs. what
was expected.

---

## Pre-push behaviour

When this skill is called before a `git push`:

1. Run all tests (steps 1–4 above).
2. If **all pass** → proceed with the push.
3. If **any fail** → do NOT push. Fix the bugs, rerun the failing tests,
   confirm they pass, then push.

Never skip tests "to save time" before a push — a broken push is worse than a
delayed one.

---

## Maintaining test cases

`test-cases.md` is the living specification. Update it:
- **When a feature is added**: add one or more test cases covering the new
  behaviour.
- **When a feature changes**: update the affected test cases.
- **When a feature is removed**: remove or disable its test cases.

Never update `SKILL.md` for feature changes — it is infrastructure, not
specification.
