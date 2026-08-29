---
name: engineering
description: "Continuous engineering standards for safe, minimal, architecture-aligned changes."
type: rule
applies_when:
  - always
priority: high
enforcement: flexible
risk: safe
source: project
version: "1.0"
---

# Engineering

## When to Use

Apply to every code, configuration, test, and documentation change.

## Requirements

- Read the nearest existing implementation and configuration before choosing a pattern.
- Make the smallest coherent change that satisfies the request; do not perform unrelated cleanup.
- Preserve public APIs, routes, DTOs, persisted data, and runtime behavior unless the task changes them.
- Keep `Backend/` aligned with `Backend/ARCHITECTURE.md`: modules own their repositories and entities, while `app` coordinates cross-module flows.
- Keep `WebAPP/` feature-oriented and use its ESLint and Prettier configuration as the style authority.
- Keep `AppPC/` compatible with Java 11 and its existing Maven dependencies.
- Add or update tests when behavior changes; run focused checks before broader checks.
- Prefer readable, explicit code over speculative abstractions or empty architectural layers.

## Allowed Exceptions

A task may override a flexible rule when the requested behavior requires it. State the exception and its impact in the handoff.

## Limitations

This rule does not authorize actions prohibited by `.agents/constraints/`.
