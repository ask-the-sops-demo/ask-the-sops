# ADR 0003: French SOPs in their own folder, linked to English by id and version

**Status:** Proposed · **Date:** 2026-11-24

## Context
The French search PRD (`docs/prd/LD-french-search.md`) needs a full French translation of each current SOP, searchable when the user picks French (`--lang fr`). We need to decide three things: where French files live, how a French file is tied to its English version, and whether translations are reviewed before staff can search them.

Options for where French files live:
1. **A `sops/fr/` folder** holding French files with the same names as the English ones. The language is clear from the folder, and the existing loader already ignores subfolders, so English behaviour can't change by accident. Downside: each SOP lives in two places that must be kept in step.
2. **A `-fr.md` file-name suffix** in `sops/`. The two languages sit side by side, which makes a missing translation easy to spot. Downside: today's loader would read French files as extra English SOPs, so the loader must change before the first French file is added.
3. **A `language` front-matter field.** Most explicit, and works wherever the file is. Downside: easy to forget, and a missing field silently defaults to a language.

Options for linking French to English:
1. **Same `id` and `version`.** No new fields. A French file is searchable only when an English file with the same id and version exists and is current.
2. **A `translation-of` field** naming the English file. Explicit, but it duplicates information already in `id` and `version` and can go stale.

Options for review:
1. **No review.** French files are AI-translated and searchable straight away. Fast and cheap. Downside: a mistranslated step (a temperature, a time limit, a count rule) reaches staff unchecked.
2. **Manager approval** before a translation becomes searchable. Safer, but adds a step and a person for every SOP change.

## Decision
French SOPs live in `sops/fr/`, with the same `id`, `version` and section numbers as the English SOP they translate. A French file is searchable only when its English counterpart exists and is current. Translations are AI-generated and are not reviewed.

## Consequences
- **Easier:** English loading and search are unaffected. Linking needs no new fields. A retired or outdated translation is excluded by the same rule that excludes retired English versions.
- **Harder:** every English SOP update needs a new French file, or French search loses that SOP until one is added. `SopLibrary` and `App` need to know about languages (`--lang fr` for `list`, `show` and `search`).
- **Watch:**
  - Unreviewed machine translation is the main risk. The English SOP is the authority: every French file and every French search result is marked as an unreviewed machine translation. If this is ever used beyond training, revisit with a new ADR that adds review.
  - French files with no matching English id and version are reported with their file name (CLAUDE.md error-message rule), not silently dropped.
  - A test checks that each French file has the same section numbers as its English version, so citations line up across languages.
  - Translation happens outside the app, so ADR 0002 (no runtime dependencies) still holds.
