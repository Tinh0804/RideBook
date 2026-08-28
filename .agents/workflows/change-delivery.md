---
name: change-delivery
description: "Orchestrate scoped feature, refactor, configuration, and documentation delivery."
type: workflow
applies_when:
  - feature-request
  - refactor-request
  - configuration-change
  - documentation-change
priority: normal
enforcement: procedural
risk: safe
source: project
version: "1.0"
skills:
  - architecture
  - backend-development-feature-development
  - frontend-dev-guidelines
  - test-driven-development
rules:
  - engineering
  - secure-coding
  - git-and-commits
  - communication-and-output
constraints:
  - repository-safety
  - secrets-and-external-access
  - resource-budgets
---

# Change Delivery

## When to Use

Use for planned implementation work that is not primarily a defect investigation, PR review, or release.

## Inputs

- Requested outcome and acceptance criteria
- In-scope subsystem and compatibility expectations
- Existing architecture, tests, and local conventions

## Sequence

1. Inspect relevant files, local instructions, worktree state, and affected contracts.
2. Bound the change and record assumptions; stop if a material product decision is missing.
3. Load only relevant skills: architecture for structural decisions, backend or frontend guidance by subsystem, and TDD when behavior is testable.
4. Plan the smallest coherent implementation and its validation.
5. Implement without modifying unrelated code or user-owned changes.
6. Run focused checks, then the smallest meaningful broader validation.
7. Review the diff for scope, security, compatibility, and accidental artifacts.
8. Hand off the outcome, changed files, validation, and remaining limitations.

## Gates and Stop Conditions

- Reconfirm before materially expanding scope.
- Apply hard constraints before any external, destructive, Git, or paid action.
- Do not convert an implementation task into deployment without a release request.

## Limitations

Technical know-how remains in the selected skills and repository documentation.
