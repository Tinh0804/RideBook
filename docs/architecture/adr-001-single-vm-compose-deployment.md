# ADR-001: Single-VM Docker Compose Deployment

## Status

Accepted

## Context

RideBook needs a repeatable deployment for a 4 GB GCP VM. The Backend is a Java 21 modular
monolith and depends on PostgreSQL and Redis. The initial production data must come from the
existing PostgreSQL 15.18 and Redis 8.6.3 containers. Keycloak is parked and is not part of the
runtime scope.

## Decision

- Run Nginx, Backend, PostgreSQL, and Redis with Docker Compose on one VM.
- Build the Backend image once in GitHub Actions and deploy the immutable image digest.
- Keep local development Compose files under `Backend/`; keep cross-service production assets
  under `deploy/`.
- Expose only Nginx. Isolate Backend and data services on Docker networks.
- Move PostgreSQL with a logical dump and Redis with an RDB snapshot instead of copying raw volumes
  across ARM64 and AMD64 hosts.
- Keep PostgreSQL 15.18 and Redis 8.6.3 during migration; upgrades are separate changes.
- Authenticate GitHub Actions to GCP with Workload Identity Federation and require a production
  environment approval.

## Rationale

This is the smallest architecture that satisfies the existing VM and cost constraints. It avoids
building on the memory-limited VM, avoids cross-architecture volume assumptions, and preserves an
application-only rollback path.

## Trade-offs

- The VM, database, and cache share one failure domain; there is no high availability.
- Backend updates can cause a short interruption because the VM cannot safely run a full blue-green
  stack within 4 GB.
- Database rollback is not automatic after new writes.
- Operational backups must leave the VM to protect against disk or instance loss.

## Consequences

- PostgreSQL and Redis need persistent volumes, backup monitoring, and tested restore procedures.
- Resource limits and Java heap settings are mandatory.
- Production releases use a GitHub approval gate and an exact image digest.
- Revisit this decision when memory stays above 75%, CPU is saturated, backup size becomes
  operationally significant, or the service requires an availability target that one VM cannot
  meet.
