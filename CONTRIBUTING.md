# Contribution Workflow

## Branches and pull requests

- Start work branches from `main`; do not push directly to `main`. Target `main` in pull requests and follow the [pull request template](.github/pull_request_template.md).
- Use `<type>/<issue-number>-<summary>`, following the convention in `danjjak`. Types: `feature`, `fix`, `docs`, `chore`.
- Human and AI contributors use the same naming convention. Do not add tool or agent prefixes such as `codex/` or `claude/`.
- Use the assigned `danjjak-app` issue number without `#`. For issue-based code migration, use the destination issue number and link the source `danjjak` issue, PR, or commit in the pull request.
- Write a short English summary in lowercase kebab-case. Omit the issue number only when the work has no related issue: `<type>/<summary>`.
- Keep one issue or cohesive feature per branch. Migrate code within the assigned issue's scope. Preserve unrelated work, update the base, and run relevant checks before review. Prefer squash merge after review and delete the merged work branch; do not overwrite shared history.
- In the PR, use `Closes #<issue-number>` only when the change completes the issue; use `Refs #<issue-number>` for partial migrations or related work.

Illustrative branch names (replace the number with the assigned issue):

```text
feature/12-spring-setup
fix/21-account-selection
docs/42-migration-guide
chore/12-gradle-wrapper
docs/branch-rules
```

## Commits and implementation

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
