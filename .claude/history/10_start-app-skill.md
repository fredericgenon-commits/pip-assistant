# 10 — `start-app` skill (local start/restart/stop)

## Prompt

> J'ai remarqué que tu refais souvent les mêmes actions lorsqu'il faut lancer
> l'application. Crée une nouvelle skill pour ce projet qui va servir pour faire
> un start ou restart du projet en local. Indique où trouver le JDK 21 et comment
> le lancer. Check si l'application tourne déjà et si c'est le cas arrête-la avant
> de faire le start. Ainsi que toute autre vérification, action, etc... nécessaire.
> Le but de cette skill est qu'un start ou restart soit simple, rapide et efficace.

## Result

New project skill `.claude/skills/start-app/` to start/restart/stop the app
locally with one command.

- **SKILL.md** — triggers on run/launch/start/restart/stop of app/backend/
  frontend ("lance l'appli", "redémarre le backend", …) or when a running
  instance is needed to verify a change. Documents ports (backend :8080,
  frontend :4200), `GET /api/health` → `{"status":"UP"}`, and JAVA_HOME =
  Corretto 21.
- **scripts/** (PowerShell, no args, absolute paths — no `cd` needed):
  - `_common.ps1` — paths, `Find-Jdk21` (auto-detects `C:\Program Files\Amazon
    Corretto\jdk21*`), port helpers (`Get-ListenerPids`, `Stop-Port`),
    `Wait-Healthy` + backend/frontend probes.
  - `status.ps1` — list PIDs listening on :8080 / :4200.
  - `stop.ps1` — port-based kill of both servers (kills the real `java`/`node`
    worker, not just the `mvnw`/`npm` launcher) + clears `.run/*.pid`.
  - `start.ps1` — launches both **detached** with logs to `.run/logs/`, runs
    `npm install` if `node_modules` is missing, waits ~120 s for health; guards
    against double-start (use `-Force` to bypass).
  - `restart.ps1` — `stop` → 2 s → `start -Force`. The usual entry point.
- `.run/` added to `.gitignore` (runtime PIDs + logs).

Verified end-to-end: `restart.ps1` brought backend and frontend to UP;
`status.ps1` showed `java`/`node` PIDs; `start.ps1` correctly refused while
running ("Use restart.ps1 to replace it").
