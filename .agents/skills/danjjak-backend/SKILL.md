---
name: danjjak-backend
description: Implement assigned Danjjak backend issues using the existing Spring Framework and MyBatis architecture after the design phase.
---

# danjjak-backend

- Read the assigned issue and its linked [backend design](../../../docs/design/backend-design.md) and feature specifications.
- Stack: JDK 17, Spring Framework 5.3, Gradle WAR, Tomcat 9, MyBatis 3.5/MyBatis-Spring 2.1, Log4j2. No Spring Boot, JPA, or `jakarta.servlet.*`.
- Preserve feature packages and `controller → service → mapper`, `dto`, `model`; see the [inventory](../../../docs/design/backend-inventory.md) only for affected files.
- During design work keep empty type declarations only. During assigned implementation use constructor injection, thin HTTP controllers, one primary feature mapper, and focused independent policy classes.
- Match mapper/XML statements. Use a Spring transaction when a mock operation needs several consistent writes; do not introduce production banking infrastructure or generic wrapper layers.
- Run relevant backend checks once the [planned environment](../../../backend/README.md) exists. Do not treat empty declarations as a runnable app.
