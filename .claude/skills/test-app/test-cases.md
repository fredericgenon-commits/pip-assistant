# Test Cases — PIP Assistant

> **Maintien** : ce fichier est mis à jour à chaque ajout ou modification de
> fonctionnalité. La skill `test-app` lit ce fichier à chaque exécution —
> ne pas modifier `SKILL.md` pour des changements fonctionnels.

---

## TC-01 — App shell : chargement et navigation

**URL :** `http://localhost:4200`

**Steps :**
1. Naviguer vers `http://localhost:4200`.
2. Attendre que la page soit chargée.

**Expected :**
- Le header contient le logo "P" et le wordmark "PIP Assistant".
- Les liens de navigation "PIPs", "Imports", "Teams", "Settings" sont visibles.
- Un bouton toggle de thème est présent dans le header.
- L'indicateur "Last synced" (heure JIRA) est visible dans le header.
- La page redirige vers `/pips` ou affiche directement la liste des PIPs.

---

## TC-02 — PIP List : affichage de la liste

**URL :** `http://localhost:4200/pips`

**Steps :**
1. Naviguer vers `http://localhost:4200/pips`.
2. Observer la page chargée.

**Expected :**
- Le titre de la section est "PIPs".
- Un sélecteur "Year filter" (mat-select) est présent avec "All" sélectionné par défaut.
- Un bouton "Refresh" est présent.
- Un bouton "New" est présent.
- Une table est affichée (même si elle est vide).

---

## TC-03 — PIP List : créer un nouveau PIP

**URL :** `http://localhost:4200/pips`

**Preconditions :** Au moins un PIP peut déjà exister.

**Steps :**
1. Naviguer vers `http://localhost:4200/pips`.
2. Cliquer sur le bouton "New".
3. Vérifier que le dialog s'ouvre avec un code PIP pré-rempli au format `YY_PIP_N`.
4. Effacer le champ et saisir un code unique de test, par exemple `99_PIP_99`.
5. Cliquer sur "Save".

**Expected :**
- Le dialog se ferme après Save.
- Le PIP `99_PIP_99` apparaît dans la liste avec le statut "PREPARATION".

**Cleanup :** (optionnel) Ce PIP de test restera dans la base H2 — c'est acceptable.

---

## TC-04 — PIP List : navigation vers PIP Details

**URL :** `http://localhost:4200/pips`

**Preconditions :** Au moins un PIP doit exister (créé en TC-03 ou préexistant).

**Steps :**
1. Naviguer vers `http://localhost:4200/pips`.
2. Double-cliquer sur la première ligne du tableau.

**Expected :**
- L'URL change vers `/pips/:id`.
- La page "PIP Details" s'affiche avec le nom du PIP et son statut.
- Un tableau de requirements est présent (peut être vide si aucun import Excel).
- Le header "Last synced: HH:mm" est visible.

---

## TC-05 — PIP Details : sélecteur d'équipe

**URL :** `/pips/:id` (naviguer depuis TC-04)

**Steps :**
1. Être sur la page PIP Details.
2. Observer le sélecteur d'équipe dans la barre d'outils.
3. Vérifier que "All" est sélectionné par défaut.
4. Changer l'équipe pour "Core".

**Expected :**
- Le sélecteur affiche "All" par défaut.
- Après changement, la colonne "Dev comment" affiche les commentaires de l'équipe Core.
- La colonne "Team Status" apparaît avec un badge pour chaque requirement.

---

## TC-06 — PIP Details : import Excel (cas nominal)

**URL :** `/pips/:id`

**Preconditions :** Générer un fichier Excel de test avec la skill `generate-test-excel`.

**Steps :**
1. Être sur la page PIP Details d'un PIP.
2. Utiliser la skill `generate-test-excel` pour créer un fichier `.xlsx` de test.
3. Glisser-déposer le fichier `.xlsx` dans la zone de drag & drop de la page.
4. Vérifier que le bouton "Import this file" devient actif.
5. Cliquer sur "Import this file".
6. Attendre que le tableau se rafraîchisse.

**Expected :**
- Après le drop, le fichier est reconnu (pas de toast d'erreur, bouton activé).
- Après import, le tableau affiche les requirements du fichier.
- Chaque ligne a une colonne "Priority" et "PIP status" (valeurs : "New" pour un premier import).
- Les colonnes de story points par équipe (Core, Portal, Process, Assets, API, Document) sont présentes.

---

## TC-07 — PIP Details : édition et sauvegarde des workloads

**URL :** `/pips/:id` (après import Excel via TC-06)

**Preconditions :** La grille contient au moins un requirement.

**Steps :**
1. Être sur la page PIP Details avec des requirements visibles.
2. Cliquer sur une cellule de story points (ex : colonne "Core" pour le premier requirement).
3. Effacer la valeur et saisir `5`.
4. Modifier une cellule de capacité dans le footer "Capacity" (ex : mettre `80` pour Core).
5. Cliquer sur "Save".

**Expected :**
- Le bouton Save répond sans erreur (pas de toast d'erreur).
- Après rechargement de la page, les valeurs `5` et `80` sont conservées.

---

## TC-08 — PIP Details : valeur TBD dans un workload

**URL :** `/pips/:id` (après import Excel)

**Steps :**
1. Cliquer sur une cellule de story points vide ou avec une valeur.
2. Effacer le contenu et saisir `TBD`.
3. Cliquer sur "Save".
4. Recharger la page.

**Expected :**
- La cellule affiche "TBD" après sauvegarde et rechargement.
- La cellule est visuellement différenciée (style TBD).

---

## TC-09 — PIP Details : statut JIRA des requirements

**URL :** `/pips/:id` (après import Excel)

**Preconditions :** Le mock JIRA est actif (profil `jira-mock`).

**Steps :**
1. Ouvrir un PIP Details avec des requirements.
2. Observer la colonne "REQ status" dans la grille.
3. Sélectionner une équipe (ex : "Core") via le sélecteur.
4. Observer la colonne "Team Status".

**Expected :**
- La colonne "REQ status" affiche un statut JIRA (ex : "TODO", "IN_PROGRESS", "DONE")
  ou un indicateur de chargement le temps de la sync.
- L'indicateur "Last synced: HH:mm" est mis à jour après quelques secondes.
- Quand une équipe est sélectionnée, la colonne "Team Status" affiche un badge
  par requirement (ex : "Ready", "Done", "TA todo", etc. ou "—" si pas de données).

---

## TC-10 — Toggle de thème Regatta / Marina

**URL :** `http://localhost:4200/pips`

**Steps :**
1. Naviguer vers la liste des PIPs.
2. Observer que le thème par défaut est "Regatta" (couleur terracotta, stripe tricolore).
3. Cliquer sur le bouton de toggle de thème dans le header.
4. Observer le changement de thème.
5. Recharger la page.

**Expected :**
- Après le premier clic, le thème passe à "Marina" (couleur navy, style moderne).
- Après rechargement, le thème "Marina" est conservé (persistance `localStorage`).
- Re-cliquer sur le toggle revient au thème "Regatta".

---

## TC-11 — PIP List : code de PIP invalide refusé

**URL :** `http://localhost:4200/pips`

**Steps :**
1. Cliquer sur "New".
2. Effacer le code pré-rempli et saisir `invalid-code`.
3. Cliquer sur "Save".

**Expected :**
- Le dialog reste ouvert.
- Un message d'erreur est affiché (format invalide, doit être `YY_PIP_N`).

---

## TC-12 — PIP Details : deep link JIRA (double-clic sur clé REQ/TCM)

**URL :** `/pips/:id` (avec requirements importés)

**Preconditions :** `pip.jira.base-url` configuré (ex : `https://jira.example.com`).
Si non configuré, les champs `reqUrl`/`tcmUrl` seront null et ce test est N/A.

**Steps :**
1. Double-cliquer sur une clé REQ dans la grille (ex : `REQ-123`).

**Expected :**
- Un nouvel onglet s'ouvre vers `<jira-base-url>/browse/REQ-123`.

---

## TC-13 — PIP Details : carte "Capacity per team"

**URL :** `/pips/:id` (avec requirements et capacités)

**Steps :**
1. Naviguer vers un PIP Details qui a des requirements importés.
2. Observer la zone des summary cards en haut de la page.

**Expected :**
- Une 4e carte "Capacity per team" est présente à droite des 3 premières.
- La carte affiche une mini-barre de progression pour chaque équipe (Core, Portal, Process, Assets, API, Doc).
- Chaque barre affiche `load/capacity` en chiffres à droite du nom de l'équipe.
- Les barres où la charge dépasse la capacité sont affichées en rouge.
- Les barres où la charge est dans la capacité sont en couleur primaire (terracotta).

---

## TC-14 — PIP Details : panneau "Last import"

**URL :** `/pips/:id` (avec au moins un import Excel effectué)

**Steps :**
1. Naviguer vers un PIP Details qui a des requirements importés (ex : 26_PIP_2).
2. Observer la zone de drop de fichier.

**Expected :**
- Un panneau "Last import" est visible à droite de la zone de drop.
- Il affiche : le numéro de version (ex : "v4"), le nom du fichier, et la date d'import.
- Un PIP sans import n'affiche pas ce panneau.

---

## TC-15 — PIP Details : label "Team comment" sur le sélecteur d'équipe

**URL :** `/pips/:id`

**Steps :**
1. Naviguer vers un PIP Details.
2. Observer le libellé à gauche du sélecteur d'équipe dans la barre d'outils.

**Expected :**
- Le libellé affiché est "Team comment" (et non "Team").
- Le sélecteur est toujours fonctionnel : changer d'équipe met à jour la colonne "Dev comment".

---

## Notes de maintenance

- **Ajouter une feature** → ajouter un TC-XX à la suite.
- **Modifier une feature** → mettre à jour le(s) TC correspondant(s) sans changer les IDs.
- **Supprimer une feature** → commenter ou supprimer le TC (garder l'historique en git).
- **TC dépendant d'un état** → toujours documenter les préconditions.
