# PRD: Search SOPs in French

**Status:** Draft · **Owner:** LD · **Last updated:** 2026-11-24

> A feature PRD for the workshop. The product, its users and all SOPs are fictional.

## Problem
Every SOP is written in English. The only French text is one summary section, in SOP-OC-05. French-speaking staff have to read and search procedures in their second language, often under time pressure. The organization also needs its SOPs to be available in both English and French.

## Users
- **French-speaking pharmacists and technicians**, who look up what to do now.
- **The pharmacy manager**, who must provide bilingual SOPs and check that staff follow the current version.

## Goals and measures
| Goal | Measure |
|---|---|
| Find the right French section fast | For the 10 most common questions asked in French, the right French section is in the top 3 results, the same target as English in v1 |
| Answers can be checked | Every French result shows the SOP id, version, section number, heading and language |
| Never follow a retired version | French search covers current versions only, as English search does |

## Scope
**In:**
- A French translation of each current SOP, produced by AI translation, as its own Markdown file in `sops/fr/` with the same id, version and section numbers (ADR 0003).
- A French SOP is searchable only while its English version with the same id and version is current. French files with no match are reported by file name.
- Every French SOP and French search result is labelled as an unreviewed machine translation.
- Choosing the language when searching, e.g. `./ask search --lang fr réfrigérateur`. English stays the default.
- Listing and showing French SOPs (`list --lang fr`, `show --lang fr SOP-CC-01`).
- French stop words (le, la, de, et, ...) so common words don't distort results.

**Out:**
- A French interface: help, messages and labels stay in English.
- French stemming and synonyms. Plurals, word forms and synonyms are not matched, as in v1. A search without accents (`refrigerateur`) will not find accented text (`réfrigérateur`).
- Languages other than English and French.

## Constraints
- **No patient information, ever.** SOPs describe procedures only, in both languages.
- Plain Java, with no new runtime dependencies (ADR 0002). Translation happens outside the app, and the app only reads the translated files.
- Files in `sops/` change only through a ticket about SOP content.

## Risks
| Risk | Mitigation |
|---|---|
| **AI translations are used without review** (decided in ADR 0003). A mistranslated step (a temperature, a time limit, a count rule) could lead staff to do the wrong thing. | Mark every French SOP as an unreviewed machine translation in the file and in search output, and keep the English version as the authority. If this is used beyond training, add review through a new ADR. |
| The French version falls behind the English one after an update | A French file must match the current English id and version, or it is not searchable, and the app warns about the mismatch |
| Staff type without accents | Known limitation, documented. A candidate follow-up story is to ignore accents when matching. |
| Section numbers or headings drift between languages, which breaks citations | Translations keep the English section numbering, and a test checks that each French file has the same section numbers as its English version |

## Open questions
- Should French search fall back to English when a French section has no match?
- Is the existing French summary in SOP-OC-05 kept, or replaced by the full translation?

**Resolved by ADR 0003:**
- *Translation review:* none. Translations are AI-generated and labelled as unreviewed.
- *Where French files live:* `sops/fr/`, linked to English by the same id and version.
