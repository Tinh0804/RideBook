---
name: secure-coding
description: "Continuous secure-coding practices for authentication, payments, user input, and external integrations."
type: rule
applies_when:
  - always
priority: high
enforcement: flexible
risk: safe
source: project
version: "1.0"
---

# Secure Coding

## When to Use

Apply continuously, with extra attention to identity, booking, finance, communication, and external integrations.

## Requirements

- Validate untrusted input at system boundaries and reject invalid state explicitly.
- Enforce authorization server-side; never rely on hidden UI controls as access control.
- Use parameterized persistence operations and established framework security APIs.
- Preserve transactionality and idempotency for booking, wallet, and payment state changes.
- Return safe client errors while retaining actionable server-side diagnostics.
- Avoid logging tokens, credentials, payment data, personal data, or raw third-party payloads.
- Keep security configuration explicit and least-privileged; do not weaken checks to make tests pass.
- Review dependency, authentication, and data-flow impact when changing external integrations.

## Escalation

If a secure implementation conflicts with the requested behavior, explain the risk and request a decision. Hard secret and external-access boundaries remain governed by constraints.

## Limitations

Use relevant security skills for technical review; this file defines policy, not a complete security manual.
