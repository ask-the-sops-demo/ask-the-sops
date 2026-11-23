---
name: adr
description: Record one architecture decision for Ask the SOPs as a numbered ADR in docs/adr/. Use when a design choice needs writing down with its reasons.
argument-hint: "[the decision to record]"
disable-model-invocation: true
---

Write an architecture decision record (ADR) for: $ARGUMENTS

1. Read `docs/adr/TEMPLATE.md`, the existing ADRs in `docs/adr/`, and any related PRD in `docs/prd/`.
2. Ask for the user's initials if you don't know them. Name the file `docs/adr/NNNN-<initials>-<short-name>.md`, where NNNN is the next number after the highest existing ADR.
3. Fill in the template:
   - **Context:** the situation, and at least two options with their trade-offs
   - **Decision:** one or two sentences
   - **Consequences:** what becomes easier, what becomes harder, and what to watch
   - **Status:** Proposed
4. Show the draft and ask for changes before saving it.
5. Do not change code. Do not commit or push.
