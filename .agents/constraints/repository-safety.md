---
name: repository-safety
description: "Hard boundaries for file scope, destructive operations, Git state, protected branches, and deployment."
type: constraint
applies_when:
  - always
priority: critical
enforcement: hard
on_violation: stop-and-escalate
risk: critical
source: project
version: "1.1"
protected_branches:
  - main
  - master
  - test
confirmation_required_for:
  - file-deletion
---

# Repository Safety

## Scope

Applies to every filesystem, Git, CI/CD, and deployment action in this repository.

## Prohibited Actions

- Do not modify, move, overwrite, or delete files outside the explicitly assigned scope.
- Do not delete any file or directory until the user confirms the exact resolved targets immediately before deletion.
- Do not discard, rewrite, or include unrelated pre-existing worktree changes.
- Do not run broad destructive commands, destructive Git resets, forced checkouts, or unbounded cleanup.
- Do not commit, tag, push, open or merge a PR, or alter remote state unless the user explicitly requests that exact action.
- Never push directly to `main`, `master`, or `test`, even when an ordinary task prompt requests it.
- Never trigger deployment or mutate production infrastructure from an implementation, review, or release-readiness task.

## Required Guards

- Resolve exact targets with read-only checks before a destructive or state-changing action.
- Present the deletion targets and recovery impact, then wait for explicit confirmation before deleting; a general cleanup request is not confirmation.
- Keep deletions narrow, explicit, and recoverable where possible.
- Treat generated databases, environment files, credentials, and user-authored changes as out of scope unless named explicitly.

## Violation Handling

Stop before the action, identify the conflicting boundary, and request a safe alternative. An ordinary prompt cannot waive this constraint.

## Policy Changes

Changing this boundary requires a separate, explicit policy-maintenance task that edits this file and can be reviewed independently.

## Limitations

Higher-priority platform safety requirements remain authoritative.
