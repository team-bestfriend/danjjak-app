# Danjjak App Agent Guide

- Project overview: [README](README.md). Current preparation: [development status](docs/development-status.md).
- Read this file, your assigned GitHub issue, and only its linked specs/design files. Use the [requirements index](docs/specs/requirements.md) only to locate missing context.
- Authority: current user instructions → revised specifications → design comments.
- This repository currently contains design only. Creating an issue does not authorize implementing it. Start functional work only when explicitly assigned for the event.
- Preserve `com.bestfriend.danjjak` feature packages and `controller → service → mapper`, with `dto` and `model`. Keep JDK 17, Spring Framework 5.3, MyBatis, Gradle WAR, Tomcat 9; no Spring Boot, JPA, or replacement domain layout.
- During the design phase, Java permits only package declarations, empty class/interface declarations, and concise Korean Javadoc. No fields, methods, constructors, imports, runtime annotations/configuration, SQL, seeds, or implementation tests.
- Korean: product/design prose, issue titles/bodies/templates, Java comments. English: agent routing/workflow instructions.
- Keep rules in their owning specification; link instead of copying them into issues. Preserve FR/UX/NFR/SC/CH/D IDs and optional/proposal status.
- Preserve unrelated work. Ask only when a missing decision materially changes scope.
- Follow [contribution rules](CONTRIBUTING.md). Do not commit secrets, local data, dependencies, or generated output. Verify only the changed scope and distinguish plans from executed results.

| Assigned work | Read when needed |
| --- | --- |
| Backend | [Backend skill](.agents/skills/danjjak-backend/SKILL.md), [package design](docs/design/backend-design.md) |
| Frontend | [Frontend skill](.agents/skills/danjjak-frontend/SKILL.md), [shared UX](docs/specs/requirements/shared-ux.md) |
| HTTP contract | [API skill](.agents/skills/danjjak-api/SKILL.md) |
| Persistence | [DB skill](.agents/skills/danjjak-db/SKILL.md), [data model](docs/design/data-model.md) |
| Patterns, transfer, FDS, analysis | [Domain skill](.agents/skills/danjjak-domain/SKILL.md) |
| Issue authoring | [Issue guide](docs/issues/guide.md) |
| Project status | [Development status](docs/development-status.md), [validation](docs/design-validation.md) |
