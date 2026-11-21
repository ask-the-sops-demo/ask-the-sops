# ADR 0002: Plain-Java keyword search for version 1

**Status:** Accepted · **Date:** 2026-11-21

## Context
Version 1 needs keyword search over a handful of SOPs (see `docs/PRD.md`). Options considered:
1. A search library such as Apache Lucene.
2. A simple TF-IDF search written in plain Java.

## Decision
Use a simple TF-IDF keyword search in plain Java (`KeywordSearch`), scoring each SOP section and counting heading words twice.

## Consequences
- No dependencies, and the whole search fits on one screen, which suits a training project.
- No stemming, synonyms or understanding of meaning. These are known limitations and planned stories.
- If the library grows to thousands of documents, or needs meaning-based search, revisit this decision with a new ADR (for example, Lucene for keyword and vector search, or RAG with an embedding model).
