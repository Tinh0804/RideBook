# Agent Instructions

## Policy Loading

- Before acting, read every `.agents/constraints/*.md`, then every `.agents/rules/*.md`.
- Match the request against workflow `applies_when`; load the most specific matching workflow.
- Load only the `SKILL.md` files named by that workflow and relevant to the affected subsystem.
- Resolve conflicts in this order: platform policy, project constraints, task request, rules, workflow, skills.
- Stop and report any hard-constraint conflict; never silently weaken a constraint.

## Project Boundaries

- RideBook contains `Backend/` (Java 21 modular monolith), `WebAPP/` (React/Vite), and legacy `AppPC/` (Java 11).
- Follow `Backend/ARCHITECTURE.md` for module ownership and dependency direction.
- Follow repository formatter, linter, Maven, npm, and CI configuration instead of duplicating style here.

## Package Managers

- Backend: use `Backend/mvnw` and the Maven reactor in `Backend/pom.xml`.
- Frontend: use npm with `WebAPP/package-lock.json`; prefer `npm ci` in clean environments.
- AppPC: use Maven with `AppPC/pom.xml` and preserve Java 11 compatibility.

## Validation

| Scope | Focused command | Full command |
| --- | --- | --- |
| Backend | `cd Backend && ./mvnw -pl <module> -am test` | `cd Backend && ./mvnw test` |
| Frontend | `cd WebAPP && npx eslint <file>` | `cd WebAPP && npm run lint && npm run build` |
| AppPC | `cd AppPC && mvn test` | `cd AppPC && mvn package` |

## Commit Attribution

- Create commits only when explicitly requested and permitted by project constraints.
- AI commits must include `Co-Authored-By: Codex <noreply@openai.com>`.
