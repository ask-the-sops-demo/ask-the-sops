# CLAUDE.md: project memory for Ask the SOPs

## What this project is
A Java command-line app that searches fictional pharmacy SOPs. It is a training project for a workshop on AI-assisted development with Claude Code, Jira and GitHub. All data is fictional.

## Stack
- Java 21, Maven 3.9, JUnit 5 (Jupiter). JaCoCo enforces a line-coverage floor (see `coverage.minimum` in `pom.xml`).
- No runtime dependencies. Do not add one without an ADR in `docs/adr/` and the reviewer's agreement.

## Commands
- Build, test and check coverage: `mvn verify`
- Tests only: `mvn test`
- Package without tests: `mvn -q package -DskipTests`
- Run: `./ask list`, `./ask show SOP-CC-01`, `./ask search <words>`
- Smoke test (used by CI): `./scripts/smoke-test.sh`

## Layout
- `sops/`: one Markdown file per SOP version, with front matter (`id`, `title`, `version`, `effective`, `status`). Sections start with `## <number> <heading>`.
- `src/main/java/ca/askthesops/`: `SopParser` (Markdown to `Sop`), `SopLibrary` (loads a folder), `KeywordSearch` (TF-IDF over sections), `App` (command line).
- `src/test/java/ca/askthesops/`: one test class per main class.
- `docs/PRD.md` and `docs/adr/`: read these before proposing design changes.

## Conventions
- Records for data (`Sop`, `Section`, `SearchResult`); small final classes for behaviour.
- Plain Java only: no frameworks.
- Every search result must carry a citation (`SOP id`, version, section number and heading).
- Error messages name the file or input that caused them.

## Workflow rules
- Work only from a Jira ticket in the `SOP` project. Branch: `feature/SOP-<n>-short-description`.
- Commit messages start with the ticket key: `SOP-<n> Short description`.
- **Tests first.** When a test already exists for a story, it is frozen: make it pass by changing the main code. **Never edit, weaken, skip or delete a test to make it pass.** If you think a test is wrong, stop and say so.
- Keep changes small and focused on the ticket.
- Pull requests use `.github/pull_request_template.md`, including the AI annotation block.
- Never push to `main`; it is protected.

## Don'ts
- No real patient, health or company information anywhere: code, tests, SOPs, commit messages, tickets.
- No secrets, tokens or passwords in the repository. Local settings go in `.env`, which is git-ignored; `.env.example` lists the names only.
- Don't change files in `sops/` unless the ticket is about the SOP content itself.
