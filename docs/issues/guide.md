# Issue Authoring Guide

- Structure: feature-group Epic → focused implementation sub-issues → internal checklists.
- Start with the assigned issue and its linked specs/design files. Consult [AGENTS.md](../../AGENTS.md) for routing; do not require a full-document reread for each task.
- Group by the existing feature boundaries. Common environment/contract/validation and shared UX have their own Epics; logging remains in `pattern` even when tracked with analysis.
- One sub-issue delivers a verifiable outcome once its prerequisites are met. Do not create an issue per Java file or automatically per requirement.
- Use Korean titles, bodies, acceptance criteria, and templates. Keep agent workflow instructions in English.
- Every implementation issue includes purpose/expected behavior, requirement IDs with owning spec links, relevant Java designs or explicitly planned locations, implementation scope, verifiable acceptance criteria, and prerequisites (or none).
- Keep detailed business rules in the owning spec. Link applicable SC and CH/D entries; do not convert scenarios into claimed results or proposals into confirmed scope.
- Use internal checklists for small tasks, edge cases, and acceptance evidence. A checklist does not replace native sub-issue relationships.

| Labels | Meaning |
| --- | --- |
| `유형:에픽` | Feature-group parent |
| `유형:구현` | Confirmed future implementation outcome |
| `유형:검토` | Evaluation or unresolved decision |
| `범위:선택` | Optional; excluded from required completion |

## Creation and dependencies

1. Inspect all existing open/closed issues by number, title, and scope before creating anything. Reuse matching work; never silently replace an existing parent.
2. Prepare the complete scope and a directed acyclic dependency graph. Use prerequisites only for work needed to implement/verify the outcome, not for staffing or arbitrary order.
3. Create/reuse Epics, then sub-issues. Establish GitHub's native parent/sub-issue relationship and native blocked-by dependencies, and list prerequisite issue links in the body.
4. Update each Epic with its actual child links. Keep optional evaluation separate in the body and do not make confirmed work depend on it.
5. Maintain the [issue map](issue-map.md) and [requirement map](requirement-map.md), covering every FR/UX/NFR/SC/CH/D ID. Track proposals and optional items explicitly.
6. Read back issue bodies, parents, dependencies, labels and states; reconcile mismatches before publication. Do not claim native relationships if only text links exist.

## Publication and implementation boundary

- Creating these future issues does not authorize functional implementation. Their checklists remain unchecked until assigned work is implemented and verified during the event.
- New issue authors should link an existing published `main` file or a verified commit. Preserve native issue relationships and update file links when documentation paths change.
- For implementation, report the environment, actual result, and evidence for linked SC scenarios; distinguish actual providers, substituted responses, and untested cases.
- D-01/02/04/05 remain design proposals in confirmed feature issues. FR-061/D-03/CH-13 has a separate optional evaluation sub-issue and must not block required Epic completion.
- Templates: [Epic](../../.github/ISSUE_TEMPLATE/epic.md), [implementation](../../.github/ISSUE_TEMPLATE/implementation.md), [decision/evaluation](../../.github/ISSUE_TEMPLATE/decision.md).
- API references: [native sub-issues](https://docs.github.com/en/rest/issues/sub-issues), [native dependencies](https://docs.github.com/en/rest/issues/issue-dependencies).
