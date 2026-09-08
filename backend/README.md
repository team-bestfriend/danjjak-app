# 백엔드 준비

Java 17·Spring Framework 5.3·MyBatis 3.5/MyBatis-Spring 2.1·Gradle WAR·Tomcat 9·Log4j2 구성을 유지합니다. [build.gradle](build.gradle)에 기본 의존성과 WAR 빌드를 구성했습니다. 현재 Java는 빈 설계 선언이며 실행 리소스 설정과 구현 테스트는 없습니다. [파일 설계](../docs/design/backend-design.md)를 보고 담당 이슈에서 환경을 구성합니다.

JDK 17과 Gradle 8.14.3을 설치한 환경에서 저장소 루트 기준으로 실행합니다. Gradle Wrapper는 아직 포함하지 않습니다.

```powershell
gradle -p backend clean build
```

생성 경로는 `backend/build/libs/danjjak.war`입니다. Servlet API는 Tomcat 9가 제공하며 WAR에서 제외합니다. 현재 WAR 생성은 설계 선언의 컴파일·패키징만 확인하며 애플리케이션 구동이나 기능 검증을 의미하지 않습니다.

[개발 이슈](../docs/issues/issue-map.md) · [개발 범위](../docs/specs/requirements/delivery-constraints.md)
