---
name: communication-and-output
description: "Continuous standards for concise, evidence-backed agent updates and handoffs."
type: rule
applies_when:
  - always
priority: normal
enforcement: flexible
risk: safe
source: project
version: "1.0"
---

# Communication and Output

## When to Use

Apply to planning, progress updates, reviews, diagnostics, and final handoffs.

## Requirements

- Match the user's language unless technical accuracy requires otherwise.
- Lead with the outcome, blocker, or decision; keep implementation narration brief.
- State material assumptions before they affect scope or behavior.
- Distinguish verified facts, test results, inferences, and unverified limitations.
- Report changed files, relevant validation commands, and remaining risks.
- For reviews, prioritize actionable findings by severity and cite precise file locations.
- Do not claim success when required validation failed or was not run.
- Never expose secret values or sensitive local data in output.

## Limitations

Formatting may adapt to the task, but hard confidentiality constraints always apply.
