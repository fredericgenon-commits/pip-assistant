# Initial Prompt

# Prompt — Création de l'application PIP Assistant

## Contexte et objectif

Nom de l'application : **PIP Assistant** (`pip-assistant`)
Package racine : `com.utmost.lu`

Dans l'entreprise, les développements logiciels sont planifiés sur des cycles
appelés **PIP** : 6 semaines de développement (3 sprints) + 1 semaine de
préparation du PIP suivant, soit des boucles de **7 semaines**.

Pour préparer un PIP, les Project Managers envoient au développement un **fichier
Excel** listant les projets du prochain PIP. Ce fichier est mis à jour
régulièrement. Chaque ligne contient :
- Le **TCM** du projet et sa description
- Le **REQ** et sa description
- Une colonne de **commentaire**
- La **charge de travail** connue, une colonne par équipe :
  Core, Portal, Process, Assets, API, Document

Le but de PIP Assistant est d'aider les Scrum Masters à gérer le backlog du
prochain PIP :
- Identifier dans JIRA les requirements et leurs tickets de développement
- Suivre les différentes mises à jour du fichier Excel
- Gérer les commentaires et actions nécessaires par équipe et par REQ

## Stack technique

### Backend (Spring Boot)
- **Spring Boot 4** (Spring Framework 7), **Java 21**, **Maven**
- Architecture **hexagonale** (ports & adapters : `domain`, `application`,
  `infrastructure`)
- Persistance : **Spring Data JPA**, base **H2** en dev, migration prévue vers
  **MS SQL Server** → utiliser **Flyway** pour les migrations dès le départ et
  éviter les types spécifiques à H2
- Sécurité : **Spring Security + JWT**, gestion **CORS** autorisant `http://localhost:4200`
- API : endpoints REST. Documentation OpenAPI (springdoc).

### Frontend (Angular)
- Dernière version stable d'Angular (standalone components, signals)
- Gestion par **npm/Node** (ce n'est PAS un projet Maven)
- UI : **Angular Material** (data-tables, tri, dialogs) + **Tailwind CSS** pour le layout

### Intégrations externes (phases ultérieures)
- GitLab REST API v4
- JIRA REST API
- XLDeploy REST API
- La configuration (URLs + tokens) se fait via `application.yml` / variables
  d'environnement (jamais commitées)

## Modèle de données (cible — pour les phases ultérieures)

> Entités au singulier. Clés primaires `id` techniques.

### Domaine métier (saisi via l'app)
- **Pip** : id, code, startDate, endDate, status
- **Team** : id, name
- **Project** : id, tcmKey, description, pip (FK)
- **Requirement** : id, reqKey, description, pmComment, project (FK)
- **Workload** : id, requirement (FK), team (FK), estimate en story points
- **PipCapacity** : id, pip, team,  capacity (story points)
- **Comment** : id, team (FK), requirement (FK), text
- **Action** : id, team (FK), requirement (FK), description, status
- **ExcelImport** : id, fileName, importDate, importedBy, version
  *(suivi des mises à jour successives du fichier Excel)*

### Données synchronisées depuis les systèmes externes
- **Ticket** : id, key (ex. `DEV-123`), summary, status,
  type (Story | Bug | Epic | TCM | Preparation task | Pre deployment task | Post deployment task),
  category (DEV | MNT | INV | REQ | TCM | REL — dérivée du préfixe de `key`, persistée),
  deliveryMethod (Project | Continuous Improvement | IT only),
  component, developer (FK), epic (FK Ticket), parent (FK Ticket), version (FK Version)
- **Developer** : id, name, jiraKey
- **Version** : id, name (tag GitLab = fix version Jira)
- **Release** : id, fixVersion (format `yyyy-nnn`, ex. `2026-001`), versions (liste)

### Catégories de tickets (règles)
| Catégorie | Rôle | Delivery method | Epic | Parent | Préfixe key |
  |-----------|------|-----------------|------|--------|-------------|
| DEV | Développement | Project, IT only | Epic (REQ) | — | `DEV-` |
| MNT | Maintenance | Continuous Improvement | Epic (MNT) | — | `MNT-` |
| INV | Investigation | Continuous Improvement | — | — | `DEV-` |
| REQ | Requirement | (toujours Project) | — | TCM | `REQ-` |
| TCM | Projet | (toujours Project) | — | — | `TCM-` |
| REL | Release | — | — | — | — (fix version `yyyy-nnn`) |

Ces categories correspondent aux "Projets" dans Jira.  Elles définissent le format des key des tickets: chaque key d'un ticket commence par sa categorie.  Exemples: DEV-512, MNT-5155, TCM-120...

### Liens entre sources
- **JIRA ↔ GitLab** : un commit est lié à un ticket si `commit.title` commence
  par `ticket.key`.
- **GitLab ↔ XLDeploy** : on déploie un tag GitLab ; ce tag est aussi la fix
  version des tickets DEV, INV et MNT dans Jira.
- **GitLab** : chaque commit appartient à une version et à un projet GitLab ; le
  projet GitLab correspond au `component` du ticket Jira. Une version peut être
  ajoutée à une release.

## Fonctionnalités
À définir dans une phase ultérieure (probablement via `/interview`).

## Livrables — Phase 1 (CE LIVRABLE : structure « hello world »)

Crée un mono-repo dans `C:\workspace\pip-assistant` contenant 2 sous-projets :
- `pip-assistant-frontend` — Angular (npm), page hello world
- `pip-assistant-backend` — Maven / Spring Boot, un seul endpoint `/api/health`

Contraintes :
- **PAS de modèle métier ni de fonctionnalités** : uniquement la structure vide
  et un « hello world » de bout en bout (le front appelle `/api/health`).
- La structure de packages hexagonale du back doit être créée (vide) :
  `com.utmost.lu.<...>.domain`, `.application`, `.infrastructure`.
- **IntelliJ** doit s'ouvrir à la racine et détecter correctement les 2
  sous-répertoires (un module Angular, un module Maven/Spring Boot).
- Crée les **run configurations IntelliJ** (versionnées dans
  `.idea/runConfigurations/`) :
    - une pour le backend (Spring Boot)
    - une pour le frontend (`npm start`)
    - une **Compound** `PIP Assistant (Full Stack)` lançant les deux ensemble
- Définis un **`.gitignore`** complet (node_modules, target, dist, .angular,
  *.env, fichiers IntelliJ sensibles — mais garde `.idea/runConfigurations/`).
  Le projet sera poussé sur GitHub.
- Crée un dossier **`/doc`** pour la documentation technique et fonctionnelle
  (Markdown).
- Crée un fichier **`CLAUDE.md`** à la racine pour consigner les informations
  importantes du projet.

## Notes Générale pour les dévelopements sur ce projet
- Utilise **context7** pour vérifier les versions exactes et la syntaxe
  (Spring Boot 4, Spring Security 7, Angular, Angular Material, Flyway, springdoc).
- Tout le **code et la documentation en anglais** ; communique avec moi en **français**.
- La documentation fonctionnelle et technique doit être mise à jour après chaque modification.
- Il faudra créer des jUnits.
- Il faudra tester l'application après chaque modification.


  ---
Changements clés vs ta version :
- Mono-repo clarifié (front = npm, pas Maven).
- Phasage : la Phase 1 ne fait que le hello-world ; le CRUD et les intégrations passent en « phases ultérieures » → plus de contradiction.
- Entités ajoutées : Pip, Workload, ExcelImport, Version, Release ; Project.tcmKey ; category persistée.
- Tableau récapitulatif des catégories de tickets (plus lisible).
- Précisions techniques : Flyway, OpenAPI, run config Compound, CLAUDE.md.


## Result

Tout est livré et vérifié. Voici le récapitulatif de la Phase 1.

✅ Phase 1 terminée — scaffold « hello world »

Structure créée dans C:\workspace\pip-assistant (mono-repo, 51 fichiers versionnés, 1er commit c38cb7d) :

pip-assistant/
├─ pip-assistant-backend/    Spring Boot 4.1.0 · Java 21 · hexagonal
├─ pip-assistant-frontend/   Angular 21 · Tailwind 4 · Angular Material
├─ doc/{functional,technical}/README.md
├─ .idea/runConfigurations/  3 configs (back, front, compound)
├─ .gitignore · CLAUDE.md · README.md

Backend : Boot 4.1.0 (starters modulaires webmvc/data-jpa/flyway), packages domain/application/infrastructure (vides), HealthController → GET /api/health, WebCorsConfig (origine 4200), application.yml (H2 + JPA ddl-auto=none), Flyway V1__init.sql.

Frontend : Angular 21 standalone/signals, Tailwind + Material, HealthService + proxy.conf.json (/api/** → :8080), page mat-card affichant le statut backend.

🔬 Vérifications (toutes passées)

┌────────────────────────┬───────────────────────────────────────────────────────────┐
│         Cible          │                         Résultat                          │
├────────────────────────┼───────────────────────────────────────────────────────────┤
│ Backend ./mvnw test    │ BUILD SUCCESS — 2/2 (contextLoads + HealthControllerTest) │
├────────────────────────┼───────────────────────────────────────────────────────────┤
│ Backend live           │ /api/health → {"status":"UP"} · /actuator/health → UP     │
├────────────────────────┼───────────────────────────────────────────────────────────┤
│ Frontend npm run build │ OK (bundle 75 kB)                                         │
├────────────────────────┼───────────────────────────────────────────────────────────┤
│ Frontend ng test       │ 2/2 (Vitest)                                              │
└────────────────────────┴───────────────────────────────────────────────────────────┘

🔧 Points résolus en cours de route (via Context7)

- Spring Boot 4 déplace @WebMvcTest → org.springframework.boot.webmvc.test.autoconfigure (corrigé).
- provideAnimationsAsync() retiré : @angular/animations n'est pas installé par Material 21 et n'est pas requis ici.
- Le frontend avait été généré dans le mauvais dossier (dérive du cwd PowerShell) → déplacé à la racine.

⚠️ À savoir / hypothèses

- J'ai utilisé Spring Boot 4.1.0 (dernière GA). Dis-moi si tu voulais figer une 4.0.x.
- Les run configs supposent un module IntelliJ nommé pip-assistant-backend (nom par défaut après import Maven) ; ajustable si IntelliJ nomme autrement.
- Pas de push GitHub : le dépôt est initialisé localement (git init -b main), prêt quand tu voudras pousser.

Prochaine étape possible quand tu veux : Phase 2 (modèle de données + migrations Flyway + CRUD), ou l'intégration JIRA/GitLab/XLDeploy.

