---
name: release
description: "Orchestrate release readiness, approval gates, and a safe CI/CD handoff without direct deployment."
type: workflow
applies_when:
  - release-request
  - deployment-readiness
  - version-preparation
priority: critical
enforcement: procedural
risk: critical
source: project
version: "1.0"
skills:
  - pre-release-review
  - git-pr-workflows-git-workflow
  - security-and-hardening
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

# Release

## When to Use

Use to assess or prepare a release. This workflow does not authorize pushing protected branches or deploying production.

## Inputs

- Release scope, target environment, and candidate revision
- Required checks, migration or configuration changes, and rollback expectations
- Human owner for the final approval and deployment action

## Sequence

1. Freeze the candidate scope and identify the exact revision and affected components.
2. Load `pre-release-review`; add Git workflow and security skills only for applicable checks.
3. Verify Backend, WebAPP, infrastructure, configuration templates, migrations, and dependency changes in scope.
4. Run or collect required build, test, lint, artifact, and CI evidence.
5. Review secrets handling, backward compatibility, rollout order, observability, and rollback feasibility.
6. Produce a readiness decision with blockers, warnings, and unresolved verification gaps.
7. Stop at the human approval gate and provide the exact CI/CD handoff steps.

## Gates and Stop Conditions

- Never push directly to `main`, `master`, or `test`.
- Never trigger `.github/workflows/cd.yml`, production APIs, payment services, or infrastructure changes.
- A failed required check is a blocker unless repository policy explicitly classifies it otherwise.
- Missing rollback or migration safety for a stateful change is a blocker.

## Output

Return `READY`, `READY WITH WARNINGS`, or `BLOCKED`, followed by evidence and the human-owned next action.

## Limitations

Release readiness does not grant deployment authority.
