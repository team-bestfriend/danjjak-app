# Contribution Workflow

- Start from `main`; use `codex/<topic>` for agent-created implementation branches and target `main` in pull requests. Follow the [pull request template](.github/pull_request_template.md).
- Keep one issue or cohesive feature per branch. Preserve unrelated work, update the base, and run relevant checks before review. Prefer squash merge after review; do not overwrite shared history.
- Commit format: `<type>: [AREA] <Korean summary> (#issue)`; issue number is optional. Types: `feat`, `fix`, `refactor`, `docs`, `test`, `build`, `chore`. Areas: `FE`, `BE`, `API`, `DB`, `INFRA`, `DOCS`.
- Respect `.editorconfig`. During the design phase, Java contains only package declarations, empty class/interface declarations, and Korean Javadoc describing responsibilities, collaborators, and requirement links.
- Backend: use `controller → service → mapper`, MyBatis `model` classes, constructor injection when implemented, and matching mapper/XML statements. Do not introduce a new ORM.
- Frontend: Vue SFCs, `PascalCase.vue`, `camelCase`, separate HTTP clients from UI. Use `views`, `components/common`, `composables`, `features`, `stores`, `router`, and `api` locations.
- During implementation, update the OpenAPI contract with endpoint changes and allocate new Flyway versions. Never edit shared/applied migrations.

| Stage | Required verification |
| --- | --- |
| Design | Local links/anchors, agent routing and skill format, complete ID-to-issue mapping, empty Java declaration grammar, prohibited file/secret checks, native GitHub relationships |
| Event implementation | Relevant FE build, BE tests/WAR, OpenAPI lint, clean DB migration, assigned SC scenarios; add commands only when tooling exists |

- Do not claim application builds or SC scenarios passed during a design-only task.
- Do not commit credentials, local configuration, dependencies, or generated output. Functional implementation requires an assigned implementation issue.
