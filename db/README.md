# DB 준비

[개념 관계](../docs/design/data-model.md)를 유지해 본선에 MySQL 8.4와 Flyway의 물리 스키마·모의 자료를 작성합니다. 현재 적용된 마이그레이션·SQL·시드는 없습니다.

[개발 이슈](../docs/issues/issue-map.md) · [개발 범위](../docs/specs/requirements/delivery-constraints.md)

## Agent Workflow

- During assigned implementation, use `db/migration/V{integer}__lowercase_description.sql`; separate schema from mock seed changes.
- Allocate the next version against the shared branch. Never edit, delete, or rename a shared/applied migration; add a new version.
- Prefer restoring an accidentally changed file before considering repair. Do not reset shared data for convenience.
- Validate the complete chain on a clean MySQL database and record the result in the issue. No database execution is authorized during the design phase.
