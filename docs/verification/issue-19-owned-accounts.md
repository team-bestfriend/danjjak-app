# #19 모의 내 계좌 불러오기 검증

## 구현 기준과 범위

- 원본: [team-bestfriend/danjjak@a296758](https://github.com/team-bestfriend/danjjak/commit/a296758aa405a67343114c34e2f375c24adb63bd).
- 요구사항: [사람·계좌](../specs/requirements/people-accounts.md), [인증·설정](../specs/requirements/auth-settings.md), [SC-016](../specs/requirements/validation-scenarios.md#sc-016-모의-계좌-불러오기).
- [공동 계약](../../contracts/openapi.yaml)의 내 계좌 목록·후보 조회·불러오기·기본 계좌 저장을 구현했습니다. 불러온 계좌 목록으로 준비 상태를 재확인하고, 첫 이용·설정·금융 화면 진입을 연결합니다.
- 지정된 account 공통 파일 7개를 원본 기준으로 이관했습니다. 모델 4개는 #13 구현을 보존하여 최종 변경이 없으며, `AccountRecord.importedAt`과 기존 `OwnedAccountImportCommand`를 유지했습니다.
- 원본에 없는 후보·저장 API와 화면을 추가했습니다. 기존 Mapper/XML을 재사용하면서 사용자 행 잠금·동의 조회·기본값 변경 구문만 추가했습니다. Flyway V1~V9와 FE 이관 파일을 통째로 덮어쓰지 않았습니다.
- 잠금은 불러오기·기본값 저장에서 동일한 사용자 행을 대상으로 합니다. READ COMMITTED 트랜잭션에서 전체 선택을 검증한 뒤 저장합니다. 첫 불러오기의 첫 선택만 기본으로 지정하고, 추가/재요청은 기존 기본·잔액·거래·패턴·불러온 시각을 보존합니다.
- 공유 오류·세션 코드와 Bean Validation 의존성, MVC 계좌 Controller 등록은 이관 코드의 실행에 필요한 범위로 보완했습니다. 상태 변경에 세션 CSRF 값을 요구하며, 선택 동의 두 값이 모두 거절이어도 완료 상태이면 계좌 기능을 사용할 수 있습니다.

## 실제 검증

환경: Windows, Eclipse Temurin JDK 17.0.18, Gradle 8.14.3, MySQL 8.4.11, Flyway 13.4.0, Tomcat 9.0.118/JDK 17. 기존 개발 DB와 다른 임시 컨테이너를 사용했습니다.

| 검사 | 결과·근거 |
| --- | --- |
| 깨끗한 DB | 기존 Flyway V1~V9 전체 적용 성공 |
| 백엔드 | `gradle -p backend test war --console=plain`, 총 28건 통과(실패·오류·건너뜀 0), WAR 빌드 성공 |
| HTTP·DB | 위 28건 중 14건은 실제 Spring RootConfig/WebConfig와 MySQL을 사용하는 [통합 테스트](../../backend/src/test/java/com/bestfriend/danjjak/account/OwnedAccountImportIntegrationTest.java) |
| 프론트엔드 | `npm test` 70건 통과, `npm run build` 성공; [계좌 화면·store·라우터 회귀 테스트](../../frontend/test/ownedAccounts.test.js) |
| 계약 | `npm run check`(contracts), OpenAPI lint 및 JSON Schema 테스트 21건 통과 |
| 브라우저 | 계좌 미준비 시 불러오기 이동, 후보 선택·실제 DB 저장·홈 이동·설정 재조회·추가 불러오기·기존 기본값 유지·명시적 기본값 변경·후보 없음·도움말 이동 확인 |
| 작은 화면 | 320×568·큰 글씨에서 발견한 고정 너비 잘림 수정, 줄바꿈·스크롤 확인 |

통합 테스트는 다음을 확인합니다.

- 불러온 본인 계좌와 후보 분리, 문자열 식별자, 번호 마스킹과 응답 필드.
- 최초 선택 순서의 기본 계좌와 ID 순서의 목록, 후보 소진.
- 같은 요청·새 테스트 세션에서 재조회해도 계좌·잔액·거래·패턴·불러온 시각 보존.
- 없는 계좌·다른 사용자 계좌·받는 계좌가 섞인 요청은 전체 취소.
- 미불러오기/받는 계좌의 기본값 저장·잔액/거래 조회 차단.
- 기본값의 명시적 변경, 다른 계좌 조회 및 추가 불러오기 후 기본값 보존.
- 동시에 첫 불러오기를 두 번 요청해도 두 계좌와 기본값 하나 유지.
- 빈/중복/잘못된/숫자 JSON 식별자, 세션·CSRF·동의 미완료 차단.
- 공통 사람 조회에서 복수 계좌 유지, 이름·관계 수정 시 계좌 데이터 보존, 계좌 필드의 무시 없는 명시적 거절.

프론트엔드는 실패 후 선택 유지·재시도, 중복 제출 잠금, 후보 없음/조회 실패 구분, 준비 전 금융 제한과 설정/도움말/로그아웃 접근, `2^53` 초과 문자열 ID, 이번 송금 선택과 저장 기본값 분리, 세션 종료 후 늦은 응답 차단을 검사합니다.

독립 코드 검토에서 발견한 사람 저장 CSRF 누락과 기본값 저장/목록 조회 실패의 경합은 수정했습니다. 기본값 저장도 전체 목록을 재조회해 확인하며, 확인 실패 시 선택을 보존하고 성공으로 표시하지 않습니다. 두 문제 모두 회귀 테스트에 포함했습니다.

## 재실행

DB 테스트는 V1~V9가 적용된 **별도 시드 검증 DB**에만 실행합니다. 개발 중인 사용자 DB를 대상으로 사용하지 않습니다. 일반 테스트는 아래 환경변수가 없으면 DB 테스트를 건너뜁니다.

```powershell
$env:DANJJAK_DB_INTEGRATION_TEST = 'true'
$env:DANJJAK_DB_URL = 'jdbc:mysql://127.0.0.1:<검증포트>/danjjak?serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true'
$env:DANJJAK_DB_USERNAME = '<검증 DB 사용자>'
$env:DANJJAK_DB_PASSWORD = '<로컬 검증 비밀번호>'
gradle -p backend test war --rerun-tasks
```

대부분의 검증 데이터 변경은 테스트 트랜잭션 종료 시 롤백합니다. 동시 요청 검사는 별도로 만든 사용자·계좌만 사용하고 종료 시 제거합니다. 비밀번호·테스트 세션 주입용 JSP·미리보기 서버·빌드 결과는 커밋하지 않습니다.

## 후속 연동과 검증 한계

- 실제 카카오 로그인·동의 저장·로그아웃 및 `/api/auth/session`의 CSRF 발급은 #17/#18 구현에 의존합니다. 브라우저 검증은 격리 Tomcat에 테스트 세션을 주입했고, 계좌 API는 실제 서버·DB를 사용했습니다. 실제 로그인부터 시작하는 E2E 성공을 주장하지 않습니다.
- 송금/패턴 서비스가 아직 설계 선언이므로 실제 송금 차감까지의 SC-005는 미검증입니다. 후속 구현에서 `TransferMapper`/`PatternMapper`의 실제 출금 선택에도 `imported_at`·소유권 검증을 적용해야 합니다. 이번 PR의 실행 제한은 FE 진입과 구현된 계좌 조회 API 범위입니다.
- 사람 생성·조회 공통 코드는 현재 FE와 이어지도록 이관했습니다. 복수 계좌를 임의로 수정하지 않도록 사람 수정은 이름·관계에 한정합니다. 사람/받는 계좌의 전체 #11 계약 적용·등록/수정·revision/requestId는 #21에서 진행합니다.
- 거래내역 응답 구조는 원본 이관 형태입니다. 거래 조회의 전체 계약 적용은 담당 후속 이슈 범위이며, 내 계좌 불러오기 계약과 구별합니다.
- 브라우저의 보호자 설정 오류는 아직 미구현인 support API 응답입니다. 계좌 기능의 성공으로 보호자·음성·금융 전체 연동을 완료했다고 간주하지 않습니다.
