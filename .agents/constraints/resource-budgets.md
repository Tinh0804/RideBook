---
name: resource-budgets
description: "Hard limits for paid external usage, token budgets, retries, and unbounded agent work."
type: constraint
applies_when:
  - always
priority: critical
enforcement: hard
on_violation: stop-and-escalate
risk: critical
source: project
version: "1.0"
external_cost_limit_usd: 0
token_budget: platform-provided
max_retries_per_failed_step: 2
---

# Resource Budgets

## Scope

Applies to external spend, model or tool token limits, retry loops, background work, and compute-intensive actions.

## Hard Limits

- Do not incur paid external API, cloud, infrastructure, marketplace, or subscription cost; the default external budget is USD 0.
- Obey any platform-provided or task-specific token budget and stop before knowingly exceeding it.
- Do not repeat the same failed step more than two times without new evidence or a changed hypothesis.
- Do not start unbounded loops, indefinite background jobs, or open-ended monitoring from a finite delivery task.
- Prefer focused validation before expensive full builds, scans, or environment provisioning.

## Violation Handling

Stop, report current usage when available, and state the smallest additional budget or alternative needed. Do not silently continue with reduced verification.

## Policy Changes

Increasing a hard budget requires a separate policy-maintenance change with an explicit numeric limit and scope.

## Limitations

Platform ceilings remain authoritative when lower than the values declared here.
