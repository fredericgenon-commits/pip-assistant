# Environment finalization & GitHub push

Follow-up prompts after the Phase 1 scaffold, about getting the project to run in
IntelliJ and publishing it.

## Prompts

1. « Le front tourne mais le backend ne se lance pas, il y a une erreur dans la run
   config. » → puis « Main class introuvable » → puis « la bannière [d'import Maven]
   n'est pas apparue ».
2. « Le backend se lance maintenant. J'ai installé Amazon Corretto 21 sur cette machine,
   tu devrais pouvoir l'utiliser pour le projet. Voici le repo GitHub distant, pousse le
   code : `git@github.com:fredericgenon-commits/pip-assistant.git`. »

## Result

**Run config error — root cause & fix.** IntelliJ had never imported the backend Maven
project (only a generic root module existed), so `src/main/java` was not a source root and
the Spring Boot run config reported *"Main class not found"*. Fix:

- declared the backend `pom.xml` in `.idea/misc.xml` (`MavenProjectsManager.originalFiles`)
  and set the project language level to JDK 21;
- the reliable manual step was **right-click `pip-assistant-backend/pom.xml` → "Add as
  Maven Project"** (the auto-import banner did not appear because the IDE was already
  open). After import the module `pip-assistant-backend` exists and the run config starts.

**Amazon Corretto 21 adopted.** Found at `C:\Program Files\Amazon Corretto\jdk21.0.11_10`.
Backend rebuilt/tested on it → `BUILD SUCCESS`, 2/2 tests. Docs (`README.md`, `CLAUDE.md`,
technical doc) updated to reference Corretto 21 as the project JDK instead of OpenJDK 22.

**GitHub push.** Committed the Corretto doc changes (`defe2be`) on top of the scaffold
(`c38cb7d`), added remote `origin` (`git@github.com:fredericgenon-commits/pip-assistant.git`)
and pushed `main` (tracking set up).

Note for IntelliJ: point **Project Structure → Project SDK** at Corretto 21.
