# 백엔드 준비

Java 17·Spring Framework 5.3·MyBatis 3.5/MyBatis-Spring 2.1·Gradle WAR·Tomcat 9·Log4j2 구성을 유지합니다. [build.gradle](build.gradle)에 기본 의존성과 WAR 빌드를 구성했습니다. PR #44에서 Spring 초기 설정과 문자열 `OK`를 반환하는 `/api/health`를 추가했습니다. 업무 구현과 DB 연결은 아직 준비 단계입니다. [공동 HTTP 계약](../contracts/README.md)과 [파일 설계](../docs/design/backend-design.md)를 보고 담당 이슈에서 구현합니다.

JDK 17과 Gradle 8.14.3을 설치한 환경에서 저장소 루트 기준으로 실행합니다. Gradle Wrapper는 아직 포함하지 않습니다.

```powershell
gradle -p backend clean build
```

생성 경로는 `backend/build/libs/danjjak.war`입니다. Servlet API는 Tomcat 9가 제공하며 WAR에서 제외합니다. WAR 생성만으로 DB 연결이나 기능 검증이 완료된 것은 아닙니다. 현재 문자열 health 응답도 DB 준비 상태를 확인하지 않으며 [#11 계약의 JSON·503 응답](../contracts/frontend-handoff.md#현재-be-실행-설정과의-차이)에 맞추는 후속 작업이 필요합니다.

[개발 이슈](../docs/issues/issue-map.md) · [개발 범위](../docs/specs/requirements/delivery-constraints.md)
