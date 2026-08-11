# Keycloak

Keycloak is deployment infrastructure, not a business module. Its custom user
storage provider remains an independent Maven JAR under `providers/user-storage`
because it runs inside Keycloak rather than the Spring Boot application.

## Database ownership

Keycloak and Backend may use the same PostgreSQL instance, but they must use
separate databases and database users:

| Owner | URL example | Purpose |
| --- | --- | --- |
| Backend | `jdbc:postgresql://postgres:5432/ridebook` | RideBook business data |
| Keycloak | `jdbc:postgresql://postgres:5432/keycloak` | Realms, clients, roles, sessions, and Keycloak users |

`KC_DB_*` configures Keycloak's own database. `RIDEBOOK_DB_*` configures the
legacy user-storage provider; Compose maps it to Backend's existing `DB_*`
values. Give the provider a read-only Backend database user when possible.
Backend must not query or migrate Keycloak tables.

The existing `data/h2` directory is legacy development data. It is not used by
the PostgreSQL configuration. Export anything valuable before removing it.

## Run locally

1. Copy `Backend/.env.example` to `Backend/.env` and replace every placeholder.
2. Run `docker compose up --build -d` from `Backend`.

The local Compose stack creates the `ridebook` and `keycloak` databases on one
PostgreSQL instance, starts Redis, then waits for healthy dependencies before
starting Keycloak and Backend. All published ports bind to localhost, while the
Keycloak management port remains inside the Docker network.

Production uses external PostgreSQL/Redis and prebuilt images:

```bash
docker compose -f docker-compose.prod.yml up -d
```

Provide a read-only Backend database account through `RIDEBOOK_DB_*` for the
legacy provider and connect the services to the external proxy network through
`DOCKER_NETWORK`.

The realm import creates the API, web, and provisioning clients, the application
roles, and the legacy account provider. Keycloak skips importing a realm that
already exists; use an explicit realm migration for later configuration changes.

Legacy accounts are read-only in the provider. Login verifies their existing
BCrypt password, while password changes continue through RideBook until those
accounts are migrated into Keycloak. Keycloak password reset applies normally to
users whose credentials are owned by Keycloak.
