---
name: git-and-commits
description: "Continuous branch, change-set, and commit-message conventions for the repository."
type: rule
applies_when:
  - always
priority: normal
enforcement: flexible
risk: safe
source: project
version: "1.0"
---

# Git and Commits

## When to Use

Apply whenever inspecting diffs, preparing a change set, or drafting a commit.

## Requirements

- Preserve unrelated and pre-existing worktree changes.
- Keep each change set focused on one purpose and include its directly related tests or documentation.
- Review the exact diff before proposing or creating a commit.
- Use `Type(Scope): Summary`, following the repository's established style.
- Prefer these types: `Add`, `Fix`, `Update`, `Refactor`, `Docs`, `Test`, and `Chore`.
- Use a short domain scope such as `Auth`, `Booking`, `Finance`, `UI`, or `Architecture`.
- Write an imperative summary without a trailing period; explain motivation in the body when needed.
- Include the agent attribution required by `AGENTS.md` when an AI-authored commit is requested.
- Do not include environment files, credentials, local databases, build output, or unrelated generated files.

## Limitations

Commit, push, merge, tag, and protected-branch permissions are hard boundaries in `repository-safety`.
