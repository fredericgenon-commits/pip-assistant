---
name: start-app
description: >-
  Start, restart or stop the PIP Assistant app locally (Spring Boot backend on
  :8080 + Angular frontend on :4200). Use this WHENEVER the user asks to run,
  launch, start, restart, relaunch or stop the app / backend / frontend / "le
  projet" / "l'appli" locally (e.g. "lance l'application", "redémarre le
  backend", "start the app", "restart everything", "arrête le serveur"), or
  whenever you yourself need a running instance to verify a change. It locates
  the Corretto 21 JDK, kills any instance already listening on :8080 / :4200
  before (re)starting, launches both servers detached with logs, and waits for
  health. Prefer this over re-deriving the launch steps by hand.
---

# Start / Restart PIP Assistant locally

Goal: make starting, restarting and stopping the app a one-command operation so
you never re-derive JDK paths, ports or launch flags by hand.

The app is two processes:

| Component | Port | Working dir              | Launched with                    |
|-----------|------|--------------------------|----------------------------------|
| Backend   | 8080 | `pip-assistant-backend`  | `mvnw.cmd spring-boot:run`       |
| Frontend  | 4200 | `pip-assistant-frontend` | `npm start` (`ng serve`)         |

The backend needs **JAVA_HOME = Amazon Corretto 21** (the scripts auto-detect
`C:\Program Files\Amazon Corretto\jdk21*`; on this machine that is
`jdk21.0.11_10`). The backend health endpoint is `GET /api/health` →
`{"status":"UP"}`. The frontend serves on `http://localhost:4200`.

H2 runs file-based with `AUTO_SERVER=TRUE`, so the DB survives restarts and the
data lives in `pip-assistant-backend/data/`.

## How to use it

All scripts are in `scripts/` next to this file and take no arguments. Run them
with the PowerShell tool — they use absolute paths derived from their own
location, so **do not `cd` first**.

- **Restart (the usual case)** — `restart.ps1` stops any running instance, then
  starts both servers and waits until they are healthy:
  ```
  & "<skill>/scripts/restart.ps1"
  ```
- **Start** (start without stopping; refuses if a port is already taken):
  ```
  & "<skill>/scripts/start.ps1"
  ```
- **Stop** everything:
  ```
  & "<skill>/scripts/stop.ps1"
  ```
- **Status** — show what is currently listening on :8080 / :4200:
  ```
  & "<skill>/scripts/status.ps1"
  ```

Replace `<skill>` with this skill's directory
(`C:\workspace\pip-assistant\.claude\skills\start-app`).

### Decision rule

- "restart / redémarre / relance" → `restart.ps1`.
- "start / lance / démarre" with nothing running → `start.ps1`; if a port is
  already busy, `start.ps1` exits with a message — switch to `restart.ps1`.
- "stop / arrête" → `stop.ps1`.
- Need a running app just to verify a change → `restart.ps1` (safe even if
  something is already up).

## What the scripts do (and how to read the result)

`start.ps1` / `restart.ps1` launch both servers **detached** (they keep running
after the tool call returns) and stream their output to log files under
`.run/logs/` at the repo root (gitignore-friendly):

- `.run/logs/backend.out` / `backend.err`
- `.run/logs/frontend.out` / `frontend.err`

The script polls `GET /api/health` (up to ~120 s — first run compiles via
Maven) and the frontend port (up to ~120 s — `ng serve` builds), then prints a
final status line per server: `UP`, or `NOT READY` with the last log lines.

If a server reports `NOT READY`, read the matching log file to see why, e.g.:

```
Get-Content "C:\workspace\pip-assistant\.run\logs\backend.err" -Tail 40
```

Common causes: port still held by an old process (run `stop.ps1` then retry),
a Flyway migration error (read `backend.out`), or `npm install` not yet run for
the frontend (the script runs it automatically on first start if
`node_modules` is missing).

## Notes

- Stopping is **port-based**: the scripts kill whatever process listens on
  :8080 / :4200 (the actual `java` / `node` worker), which is more reliable than
  killing the `mvnw`/`npm` launcher since those spawn the real server as a
  child. PIDs are also recorded in `.run/*.pid` as a fallback.
- These are dev-only helpers. Do not use them to manage a production instance.
- If the Corretto folder is missing or not a `jdk21*`, `start.ps1` fails fast
  with a clear message — update the JDK or the detection glob in the script.
