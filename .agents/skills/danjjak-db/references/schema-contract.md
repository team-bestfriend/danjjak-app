# Schema Routing

- Use [data relationships](../../../../docs/design/data-model.md) and the assigned feature spec as the design authority.
- The [Java design catalog](../../../../docs/design/backend-inventory.md) describes MyBatis model and mapper responsibilities.
- Define concrete columns and constraints in the assigned implementation issue.
- Preserve person/account separation, nullable direct-transfer execution links, historical snapshots, current consent choices, and target-specific audio metadata.
- A schema addition needs persisted behavior that existing state or queries cannot represent. Do not add aggregate report tables, guardian accounts, or raw event/audio storage.
- Follow [Flyway workflow](../../../../db/README.md) once implementation is authorized.
