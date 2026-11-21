# Ask the SOPs

A small Java command-line app that searches a library of **fictional** pharmacy standard operating procedures (SOPs). It is the practice project for the workshop *Building with Claude Code, Jira and GitHub*.

> **Training only.** Every SOP in `sops/` is fictional and written for a software workshop. It is not clinical or regulatory guidance. Never add real patient, health or company information to this project.

## Quick start

Inside Ubuntu (WSL), from the project folder:

```bash
mvn verify                      # build, run the tests and check coverage
./ask list                      # the current SOPs
./ask list --all                # every version, including retired ones
./ask show SOP-CC-01            # one SOP, section by section
./ask search overnight refrigerator excursion
```

`./ask` builds the app the first time you run it. Run `mvn -q package -DskipTests` again after you change the code.

## What's here

| Path | What it is |
|---|---|
| `sops/` | The fictional SOPs, one Markdown file per version |
| `src/main/java/ca/askthesops/` | The app: parser, library, keyword search, command line |
| `src/test/java/ca/askthesops/` | JUnit 5 tests |
| `docs/PRD.md` | What the product is for, and what's in and out of scope |
| `docs/adr/` | Architecture decision records |
| `CLAUDE.md` | Project memory for Claude Code: stack, commands, rules |
| `.github/` | Pull request template and GitHub Actions workflows |
| `scripts/` | The workshop setup check and the smoke test |
| `.devcontainer/` | The GitHub Codespaces backup environment |

## How we work

1. **Jira first.** Every change starts from a ticket in the `SOP` project.
2. **Branch** from `main` as `feature/SOP-<n>-short-description`.
3. **Test first.** Write a failing test, then make it pass. Don't change a test to make it pass.
4. **Commit** with the ticket key first: `SOP-12 Add synonyms to keyword search`.
5. **Open a pull request** with the key in the title, and fill in the template, including the AI annotation.
6. **CI must pass and one person must approve** before anything merges into `main`.
7. Merges to `main` go to **staging**. A tagged release (`v1.2.0`), approved by a named person, goes to **production**.

## Known limitations (good first stories)

The keyword search is simple on purpose. Each of these makes a good story to practise the workflow:

- **No synonyms:** "fridge" finds nothing, because the SOPs say "refrigerator".
- **No stemming:** "excursions" and "excursion" are different words to the search.
- **No meaning:** "can a tech count narcotics alone?" works only because the words happen to match.
- **Only the current version is searched.** There's no way yet to search a retired version on purpose.

The last part of the workshop plans how **RAG** (retrieval-augmented generation) can address the third one.
