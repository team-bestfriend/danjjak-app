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
- Follow [contribution rules](CONTRIBUTING.md), including the shared branch naming convention for human and AI contributors. Do not commit secrets, local data, dependencies, or generated output. Verify only the changed scope and distinguish plans from executed results.

## Branch rules for agents

- Use `<type>/<issue-number>-<summary>` for all agent-created work branches, including delegated work and worktrees. Types: `feature`, `fix`, `docs`, `chore`.
- Do not use AI tool or agent names as branch prefixes, including `codex/` and `claude/`. Human and AI contributors follow the same convention.
- Use the assigned `danjjak-app` issue number without `#`, including when migrating code from `danjjak`. Link the source issue, PR, or commit in the pull request.
- Use a short English lowercase kebab-case summary, for example `feature/12-spring-setup`. Only omit the issue number when no related issue exists, for example `docs/branch-rules`.
- Start new work branches from `main`, keep each branch within one issue or cohesive feature, and target `main` in pull requests. Reuse an existing branch for the assigned work when appropriate; do not rename or switch another agent's active branch.
- Apply these naming rules without requesting confirmation for routine branch naming. Follow the [contribution workflow](CONTRIBUTING.md) for review, verification, and merging.

## Work routing

| Assigned work | Read when needed |
| --- | --- |
| Backend | [Backend skill](.agents/skills/danjjak-backend/SKILL.md), [package design](docs/design/backend-design.md) |
| Frontend | [Frontend skill](.agents/skills/danjjak-frontend/SKILL.md), [shared UX](docs/specs/requirements/shared-ux.md) |
| HTTP contract | [API skill](.agents/skills/danjjak-api/SKILL.md) |
| Persistence | [DB skill](.agents/skills/danjjak-db/SKILL.md), [data model](docs/design/data-model.md) |
| Patterns, transfer, FDS, analysis | [Domain skill](.agents/skills/danjjak-domain/SKILL.md) |
| Issue authoring | [Issue guide](docs/issues/guide.md) |
| Project status | [Development status](docs/development-status.md), [validation](docs/design-validation.md) |
