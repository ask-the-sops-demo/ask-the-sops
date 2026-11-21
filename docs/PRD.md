# PRD: Ask the SOPs, version 1 (keyword search)

**Status:** Accepted · **Owner:** Workshop team · **Last updated:** November 2026

> An example product requirements document for the workshop. The product and its users are fictional.

## 1. Problem
Pharmacy staff need to find the right procedure quickly, often under time pressure (a refrigerator alarm at opening, a count that doesn't match). Today the SOPs are long documents in a shared folder. Finding the right section means opening several files and scrolling.

## 2. Users
- **Pharmacists and pharmacy technicians** looking up what to do now.
- **The pharmacy manager**, who maintains the SOPs and checks that staff follow the current version.

## 3. Goals and success measures
| Goal | Measure |
|---|---|
| Find the right section fast | For the 10 most common questions, the right section is in the top 3 results |
| Always follow the current version | Search never returns a retired version |
| Answers can be checked | Every result shows the SOP id, version and section number |

## 4. Scope
**In scope (v1):**
- Load SOPs from Markdown files, including retired versions.
- List current SOPs; show one SOP by section.
- Keyword search over the current versions, with a citation for every result.

**Out of scope (v1):**
- Synonyms, stemming or any understanding of meaning.
- Answering questions in sentences (a possible v2 using RAG).
- A web interface, user accounts, editing SOPs in the app.

## 5. Constraints
- **Privacy:** no patient or personal health information, ever. SOPs describe procedures only.
- **Simplicity:** plain Java, no external services, runs on a laptop.

## 6. Risks
| Risk | Mitigation |
|---|---|
| Staff search with everyday words that aren't in the SOPs ("fridge") | Known limitation; candidate for v1.1 (synonyms) and v2 (RAG) |
| A retired version is followed by mistake | Search covers current versions only; `list --all` labels retired ones |

## 7. Open questions
- Should a retired version ever be searchable, for audits?
- Is French needed for all SOPs, or only for summaries?
