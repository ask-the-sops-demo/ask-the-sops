---
name: prd
description: Draft a short product requirements document (PRD) for a proposed improvement to Ask the SOPs. Use when someone wants to plan a feature or change before any code is written.
argument-hint: "[short description of the idea]"
disable-model-invocation: true
---

Help plan this improvement to Ask the SOPs: $ARGUMENTS

1. First read `CLAUDE.md`, `docs/PRD.md` and the ADRs in `docs/adr/`.
2. Interview the user before writing anything. Ask one question at a time until you understand the problem, who has it, how success will be measured, and what is out of scope. Ask no more than six questions.
3. Ask for the user's initials, then write `docs/prd/<initials>-<short-name>.md` with these sections:
   - **Problem:** what is hard today
   - **Users:** who has the problem
   - **Goals and measures:** at least one goal you could test, such as "the right section is in the top 3 results"
   - **Scope:** what is in, and what is out
   - **Constraints:** including "no patient information, ever"
   - **Risks**
   - **Open questions**
4. Keep it under one page, in plain language.
5. Do not change code or any other file. Do not commit or push.
6. Finish by listing the design decisions that might need an ADR (`/adr`) and suggesting the next step (`/story`).
