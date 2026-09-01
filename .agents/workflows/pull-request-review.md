---
name: pull-request-review
description: "Orchestrate a read-only, evidence-backed review of a branch, diff, or pull request."
type: workflow
applies_when:
  - pull-request-review
  - code-review
  - diff-review
priority: high
enforcement: procedural
risk: safe
source: project
version: "1.0"
skills:
  - git-pr-review
  - code-review-and-quality
  - security-and-hardening
  - pre-release-review
rules:
  - engineering
  - secure-coding
  - communication-and-output
constraints:
  - repository-safety
  - secrets-and-external-access
  - resource-budgets
---

# Pull Request Review

## When to Use

Use for review-only requests concerning a PR, branch comparison, patch, or working-tree diff.

## Inputs

- Review target and base reference
- Intended behavior or linked acceptance criteria
- Available CI, test, and build evidence

## Sequence

1. Establish the exact base, head, diff, and pre-existing worktree changes.
2. Read affected architecture, contracts, tests, and local conventions.
3. Load the PR and code-quality skills; add security or pre-release review only when the diff warrants them.
4. Trace changed behavior for correctness, regressions, authorization, data integrity, and compatibility.
5. Validate suspected findings with code paths or focused read-only checks.
6. Rank findings by impact and confidence; include precise file locations and remediation direction.
7. Summarize verification gaps after findings; do not bury defects in general commentary.

## Gates and Stop Conditions

- Remain read-only unless the user separately asks for fixes.
- Do not submit reviews, comments, approvals, or remote status changes without explicit authorization.
- Do not report style preferences already enforced by tooling as defects.

## Output

Lead with findings ordered by severity. If none are found, state that explicitly and list residual test or coverage risks.

## Limitations

A clean review is not proof that the change is defect-free.
