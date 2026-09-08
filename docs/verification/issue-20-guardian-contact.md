# #20 보호자 연락처·전화 연결 검증

## 이관 기준과 변경 범위

- 원본: [team-bestfriend/danjjak@a296758](https://github.com/team-bestfriend/danjjak/commit/a296758aa405a67343114c34e2f375c24adb63bd). 최신 main과 디렉터리 전체 복사를 사용하지 않았습니다.
- 근거: [FR-009](../specs/requirements/people-accounts.md), [FR-043·카카오 시연](../specs/requirements/fds-guardian.md), [SC-010](../specs/requirements/validation-scenarios.md#sc-010-보호자고객센터-전화), [공동 API 계약](../../contracts/openapi.yaml).
- 지정된 support Controller·DTO·Service·모델·Kakao client 8개를 파일별로 이관했습니다. `GuardianContactRecord`는 원본과 동일하여 최종 diff가 없고, `NotificationAnomalyRecord`에는 안전한 알림 본문에 필요한 은행·계좌·사유 필드를 추가했습니다.
- Controller·DTO·Service는 현재 응답 계약, 세션·CSRF·동의 선택 완료, 전화번호 문자열 검증, 본인 시연 확인에 맞췄습니다. 선택 동의 둘을 거절해도 연락처 관리가 가능합니다. `build.gradle`과 공통 DTO·오류·세션 파일은 그대로 유지했습니다.
- #13 `SupportMapper`와 XML의 기존 구문은 보존했습니다. 동의 완료 조회와 소유자 범위의 판정 행 잠금 조회만 추가했습니다. #12 설정에는 support MVC 스캔 1개와 환경변수 설정 4개만 병합했습니다.
- 기존 Flyway V1~V9는 변경하지 않았습니다. 새 V10은 `guardian_contacts.phone_number` 길이를 현재 계약과 같은 30자로 확장합니다.
- #42 프런트엔드는 재복사하지 않았습니다. 기존 설정 화면·경고 화면·고객센터 소비부와 API/store를 현재 support 계약에 연결했습니다. 전화 확인·입력 초안·세션 간 늦은 응답 보호를 추가했습니다.
- #34/#39에서 support 백엔드를 다시 복사할 필요가 없습니다. 후속 작업은 고객센터·송금·인증 연결과 제공사·실기기 검증입니다.

## 실제 검증

환경: Windows, Eclipse Temurin JDK 17.0.18, Gradle 8.14.3, MySQL 8.4.11, Flyway 13.4.0, Tomcat 9.0.118/JDK 17. 사용자 개발 DB와 분리된 임시 컨테이너에서 실행했습니다.

| 검사 | 결과 |
| --- | --- |
| 깨끗한 DB | Flyway V1~V10 전체 적용 성공 |
| 백엔드 | 기준 `ab964a1` + #20 변경에서 `gradle -p backend test war --console=plain`: 44건 통과(실패·건너뜀 0), WAR 성공. 이후 main의 컴파일 차단은 아래에 별도 기록 |
| HTTP·DB | [SupportIntegrationTest](../../backend/src/test/java/com/bestfriend/danjjak/support/SupportIntegrationTest.java) 10건. 기존 계좌 14건도 함께 통과 |
| 외부 클라이언트 | [KakaoMemoMessageClientTest](../../backend/src/test/java/com/bestfriend/danjjak/support/KakaoMemoMessageClientTest.java): 로컬 HTTP 요청 형식, 확정 실패·잘못된 응답·시간 초과 구분, 자동 재전송 없음 |
| 프런트엔드 | `npm test` 103건 통과, `npm run build` 성공; [support 회귀 테스트](../../frontend/test/support.test.js)로 설정·전화 확인·알림·CSRF·비동기 응답 검증 |
| 계약 | `npm run check`(contracts): OpenAPI lint 및 JSON Schema 테스트 21건 통과. GET support의 동의 미완료 409를 기존 오류 계약 참조로 보완 |
| 브라우저 | 실제 WAR/DB에 시험 번호 저장, 성공 표시, 새로고침 후 같은 번호 확인, 잘못된 하이픈 입력 거절과 초안 표시 확인 |
| 작은 화면 | 320×568·큰 글씨에서 번호·안내 줄바꿈과 가로 잘림 없음 확인 |

백엔드 통합 테스트는 번호 원문·5/30자 경계·사용자당 한 연락처·다른 사용자 격리, 잘못된 JSON 타입·문자·길이·추가 필드 거절을 확인합니다. 연락처 변경 시 계좌·잔액·거래·패턴은 보존됩니다.

알림은 본인 소유·미결정·HIGH·공유 동의·명시적 시연 확인을 모두 요구합니다. 실제 성공만 UTC 초 단위 시각을 저장하며 미결정 시 재요청에 같은 결과를 반환합니다. 동시에 요청해도 판정 행 잠금으로 실제 client 호출을 한 번만 수행합니다. 모의 결과와 불명확한 결과에는 실제 전송 시각을 저장하지 않습니다. 시간 초과/연결 단절 등 결과를 알 수 없으면 502 `NOTIFICATION_RESULT_UNKNOWN`으로 안내하고 자동 재전송하지 않습니다.

본문에는 본인 전송 시연·두 판정 사유·수취인·금액·마스킹 계좌를 포함합니다. 전체 계좌번호·PIN·보호자 전화번호를 수신자나 본문에 넣지 않습니다. 전화·알림은 차감·거래 생성·최종 송금 결정·보호자 승인으로 처리하지 않습니다.

프런트엔드 검증은 저장 실패 후 초안 유지, 저장 중 중복 제출 방지, 이전 조회의 늦은 응답과 세션 변경 격리, 명시적 번호 확인 후에만 `tel:` 제공, 미등록 안내·통화 미지원 시 읽을 수 있는 번호, 본인 전송 확인 및 실제/모의/불명확 결과 표시를 포함합니다.

독립 검토에서 발견한 GET support의 409 응답 누락, 정식 동의 필드와 기존 이관 필드 차이, 알림 중 송금 결정 가능, 이전 세션의 401이 새 세션을 종료하는 문제를 수정했습니다. 정식 `guardianSharing`을 우선하며 필드가 없을 때만 기존 `guardianShareAgreed`를 읽습니다. 알림 대기 중 재확인·계속·취소는 화면과 store 양쪽에서 잠그고 종료 후 해제합니다. 전화 번호 확인은 계속 사용할 수 있습니다.

## 최신 main의 병합 차단

작업 중 추가된 `origin/main@0887a6f`를 별도로 추출하여 `compileJava`를 실행했으나 실패했습니다. #20 변경이 없는 main 자체에서 [#18 PR #50](https://github.com/team-bestfriend/danjjak-app/pull/50)의 `UserController`가 참조하는 `UserDtos.AccessibilitySettings`, `ConsentSettings`, `ConsentUpdateRequest`, `CurrentUserResponse`를 찾지 못합니다. `UserDtos`와 `UserService`는 아직 빈 선언입니다.

따라서 위 백엔드 44건 통과는 이전 기준 `ab964a1`에 #20을 적용한 결과이며, 최신 main 전체 검증 성공을 의미하지 않습니다. #20 PR은 초안으로 남겨 해당 선행 이관이 완성된 뒤 최신 main에서 테스트·WAR·기동을 재확인하고 병합합니다. 다른 이슈의 파일을 임의로 되돌리거나 #20에서 통째로 재이관하지 않습니다.

## 실행 설정과 남은 검증

다음 값은 배포 환경에서 제공합니다. 실제 고객센터 번호를 임의로 만들어 기본값에 넣지 않습니다.

| 환경변수 | 용도 |
| --- | --- |
| `DANJJAK_SUPPORT_CUSTOMER_CENTER_PHONE` | 제공된 고객센터 번호. 비어 있거나 형식이 잘못되면 GET support는 503 |
| `DANJJAK_SUPPORT_CUSTOMER_CENTER_NAME` | 제공된 고객센터 이름. 생략 시 ‘고객센터’ |
| `DANJJAK_KAKAO_MESSAGE_URL` | 제공사의 나에게 보내기 엔드포인트 |
| `DANJJAK_KAKAO_MESSAGE_LINK_URL` | 메시지에 사용할 등록된 서비스 링크 |

세션의 Kakao 토큰 또는 메시지 설정이 없으면 실제 호출 없이 모의 결과를 반환합니다. 검증에서는 외부 자격정보를 사용하지 않았으며 실제 카카오 메시지나 전화는 발신하지 않았습니다.

실제 로그인·동의 백엔드와 송금 경고 생성은 아직 skeleton입니다. 브라우저 검증은 임시 Tomcat에만 주입한 테스트 세션과 빈 패턴 응답을 사용했고, support 조회·저장은 실제 WAR/DB로 수행했습니다. 전화 확인 화면은 Vue 컴포넌트 테스트로 검증했습니다. 실기기의 전화 앱 전환과 실제 Kakao 제공사 전송, 인증→실제 송금 경고→전화의 전체 SC-010은 후속 연결 후 확인해야 합니다. 따라서 #20을 자동 종료하지 않습니다.

Kakao 전송과 DB 커밋은 하나의 원자적 작업이 될 수 없습니다. 외부 성공 직후 프로세스/DB가 중단되는 경우와 별도 송금 결정 서비스의 동시성은 #39 통합 검증에서 추가로 확인합니다. 이 작업은 자동 재발송이나 별도의 모의 전송 이력을 도입하지 않습니다.

DB 테스트는 V1~V10을 적용한 별도 시드 검증 DB에만 실행합니다.

```powershell
$env:DANJJAK_DB_INTEGRATION_TEST = 'true'
$env:DANJJAK_DB_URL = 'jdbc:mysql://127.0.0.1:<검증포트>/danjjak?serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true'
$env:DANJJAK_DB_USERNAME = '<검증 DB 사용자>'
$env:DANJJAK_DB_PASSWORD = '<로컬 검증 비밀번호>'
gradle -p backend test war --rerun-tasks
npm --prefix frontend test
npm --prefix frontend run build
npm --prefix contracts run check
```

일반 실행에서는 환경변수가 없으면 DB 테스트를 건너뜁니다. 테스트는 롤백하거나 자신이 만든 동시성 검증 데이터만 제거합니다. 검증 비밀번호·세션 주입 JSP·미리보기 서버·의존성·빌드 결과는 커밋하지 않습니다.
