# Documentation cleanup & prompt-history convention

## Prompt

« J'ai ajouté un folder `.claude/history` dans lequel j'ai créé un fichier
`01_initial_prompt` avec le prompt de création de l'application. Je voudrais que tu
enregistres l'historique des prompts de cette façon. Pour les prompts suivants, on peut
regrouper les dialogues par jour ou par fonctionnalité (je ne sais pas quelle est la
meilleure solution). J'aimerais aussi simplifier la documentation : pas de sous-folder
`functional` et `technical`, uniquement 2 fichiers `functional.md` et `technical.md`.
Pour la documentation, n'hésite pas à intégrer des schémas Mermaid si besoin. »

## Result

**Prompt-history convention** (documented in `CLAUDE.md` → "Prompt history"): files live
under `.claude/history/`, numbered `NN_<feature-slug>.md`, **grouped by feature/phase**
(recommended over by-day, since a feature spans days and a day spans features). Each file =
title + prompt(s) + `## Result`. Created `02_environment-and-github.md` (run-config fix +
Corretto 21 + GitHub push) and this `03_*` file; appending continues an existing feature.

**Documentation simplified.** Replaced `doc/functional/README.md` and
`doc/technical/README.md` with single files `doc/functional.md` and `doc/technical.md`;
removed the now-empty subfolders. Updated links in `README.md` and `CLAUDE.md`.

**Mermaid diagrams added:**
- `functional.md` — PIP 7-week cycle (gantt), domain model (erDiagram), project ticket
  hierarchy TCM→REQ→DEV (flowchart), and JIRA/GitLab/XLDeploy cross-source links.
- `technical.md` — monorepo + dev-proxy architecture, hexagonal layers, and the
  `/api/health` round-trip sequence diagram.

`.claude/settings.local.json` is git-ignored; `.claude/history/` is versioned.
