---
name: danjjak-db
description: Plan or implement assigned MyBatis persistence and Flyway work while preserving Danjjak mock-finance data relationships.
---

# danjjak-db

- Read the assigned issue and [schema routing](references/schema-contract.md); use the [data model](../../../docs/design/data-model.md) for conceptual relationships.
- This project has no applied migrations or physical schema. Define concrete persistence details in the assigned implementation issue; mock seed counts are not account limits.
- Preserve the existing model where it represents current behavior. Explain why existing state/query logic is insufficient before adding persisted data.
- During implementation use MySQL 8.4 and Flyway; separate schema and mock seed migrations, allocate versions, and never modify shared/applied files. See [DB plan](../../../db/README.md) and [environment plan](../../../infra/README.md).
- Keep raw PINs, OAuth tokens, audio binaries, command text/audio, and real credentials out of business tables/migrations/logs. Store audio references with ownership metadata.
- No SQL, seeds, or DB execution during the design phase. Validate the migration chain on a clean database only in the assigned implementation issue.
