---
name: danjjak-api
description: Define and maintain concrete HTTP contracts for assigned Danjjak implementation issues.
---

# danjjak-api

- Read the assigned issue and linked feature input/output/error rules plus [contract plan](../../../contracts/README.md).
- There is no executable OpenAPI file in this design scaffold. Create `contracts/openapi.yaml` during the assigned event issue; it then owns concrete HTTP paths, fields, and status codes.
- Keep FE/BE and error contracts synchronized; do not create a competing Swagger annotation contract or unused schemas.
- Settle person/account identity, guidance target identity, partial-save retry identity, selected source, consent, and financial/last-visit finalization ownership before dependent implementation.
- Do not generate clients/servers unless requested. Lint the contract with the version chosen in the environment issue once it exists.
