# ADR 0001: Record architecture decisions

**Status:** Accepted · **Date:** 2026-11-21

## Context
Design decisions get lost in chat threads and memories. New team members, reviewers and Claude Code all need to know *why* the code is the way it is.

## Decision
We record each significant decision as a short Markdown file in `docs/adr/`, numbered in order, using `TEMPLATE.md`. CLAUDE.md points to this folder, so Claude Code reads the decisions before proposing changes.

## Consequences
- Decisions are reviewed in pull requests, like code.
- A decision is never edited after it's accepted. To change it, add a new ADR that supersedes it.
