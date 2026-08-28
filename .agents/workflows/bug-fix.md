---
name: bug-fix
description: "Orchestrate evidence-driven diagnosis, minimal repair, and regression verification."
type: workflow
applies_when:
  - bug-report
  - test-failure
  - runtime-error
  - unexpected-behavior
priority: high
enforcement: procedural
risk: safe
source: project
version: "1.0"
skills:
  - systematic-debugging
  - diagnosing-bugs
  - test-fixing
  - security-and-hardening
rules:
  - engineering
  - secure-coding
  - communication-and-output
constraints:
  - repository-safety
  - secrets-and-external-access
  - resource-budgets
---

# Bug Fix

## When to Use

Use when behavior is incorrect, a test fails, or an error needs diagnosis and repair.

## Inputs

- Reproduction steps, expected behavior, and observed behavior
- Logs or failing tests with sensitive values removed
- Suspected scope, environment, and last known good state when available

## Sequence

1. Confirm the symptom and establish a reproducible or evidence-backed failure.
2. Load `systematic-debugging`; add specialized diagnosis or security skills only when relevant.
3. Trace the failing path and identify the root cause before editing production code.
4. Add or identify a regression check that fails for the confirmed cause.
5. Implement the smallest root-cause fix without masking errors or weakening safeguards.
6. Run the regression check, nearby tests, and affected build or lint checks.
7. Inspect the diff for collateral behavior changes and security regressions.
8. Report root cause, repair, evidence, and anything not reproduced or verified.

## Gates and Stop Conditions

- After two failed hypotheses with the same evidence, stop repeating and reassess.
- Do not change expected behavior merely to make a failing test pass without authorization.
- Treat environment-only or production-only actions as external-access decisions.

## Limitations

When reproduction is impossible, clearly label the repair as hypothesis-based.
