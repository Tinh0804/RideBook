---
name: secrets-and-external-access
description: "Hard boundaries for secret handling and authenticated or mutating external services."
type: constraint
applies_when:
  - always
priority: critical
enforcement: hard
on_violation: stop-and-escalate
risk: critical
source: project
version: "1.1"
authenticated_or_mutating_api_allowlist: []
protected_env_patterns:
  - "**/.env"
  - "**/.env.*"
protected_env_exceptions:
  - "**/.env.example"
confirmation_required_for:
  - read-protected-env-file
---

# Secrets and External Access

## Scope

Applies to credentials, personal data, environment files, third-party APIs, cloud services, and remote systems.

## Prohibited Actions

- Do not reveal, copy into output, commit, or persist secret values, tokens, private keys, passwords, or production personal data.
- Do not read `.env` or environment-specific variants such as `.env.local` and `.env.production` without the user's explicit permission for the exact file.
- Do not replace real secrets with fabricated values in tracked configuration; use documented placeholders in example files.
- Do not call an authenticated or mutating external API unless its host and operation are explicitly allowlisted in this file.
- Do not call live VNPay, MoMo, Firebase, Google Maps, OAuth, email, notification, or production endpoints for testing.
- Do not upload repository content, logs, database files, or user data to external services.

## Allowed Read-Only Access

Public documentation, package metadata, and `.env.example` templates may be read when platform policy permits. This does not authorize real environment files, authenticated APIs, downloads with side effects, or external data submission.

## Required Guards

- Redact sensitive values from commands, logs, screenshots, fixtures, and handoffs.
- Before reading a protected environment file, name its exact path and purpose, ask the user, and wait for confirmation; permission for one file does not cover another.
- After permission is granted, read only the minimum required content and never reproduce secret values in output.
- Use `.env.example`, mocks, local services, or sandbox endpoints for development and verification.
- Treat the empty API allowlist as default-deny for authenticated and mutating operations.

## Violation Handling

Stop before the external action and identify the exact host, operation, data exposure, and safer substitute.

## Policy Changes

Allowlist changes require a separate, explicit policy-maintenance task and a narrowly defined host and operation.

## Limitations

Platform sandbox and network restrictions may be stricter than this project policy.
