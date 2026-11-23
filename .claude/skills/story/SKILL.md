---
name: story
description: Turn a PRD or an idea for Ask the SOPs into small Jira stories with Given/When/Then acceptance criteria, and create them in the SOP project after the user approves.
argument-hint: "[PRD file or idea]"
disable-model-invocation: true
---

Plan Jira stories for: $ARGUMENTS

1. If a PRD file is named, read it, plus any related ADRs in `docs/adr/`.
2. Draft two to five stories. Each one has:
   - a short title that starts with a verb
   - "As a <role>, I want <capability>, so that <benefit>."
   - two to four acceptance criteria in Given / When / Then form, at least one of which protects behaviour that works today
   - one line on which test would prove it
3. Keep each story small enough to finish in a few days.
4. Show all the drafts and wait for the user's edits.
5. When the user approves, create them in the `SOP` Jira project with the Atlassian tools, as Story issues, with the acceptance criteria in the description. Show exactly what you will send before creating anything.
6. Report the keys Jira assigned, and suggest a branch name for the first one: `feature/<KEY>-short-description`.
7. Do not change code. Never put patient information in a ticket.
