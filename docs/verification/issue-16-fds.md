# #16 FDS 추가 검증

## 결과와 종료 판단

2026-09-08, `main@a6134e1`에서 실제 MySQL 검증 13건과 기존 정책 검증 8건, **21건 모두 통과**했습니다. 실패·오류·건너뜀은 0건입니다. 이 결과는 정책과 Mapper의 실행 검증이며 실제 송금 HTTP 흐름이나 전체 WAR 검증이 아닙니다.

**#16은 아직 종료하지 않습니다.** `TransferService`가 빈 선언이어서 직접/패턴 송금이 `countRecentTransfers`, `FdsEvaluator`, `insertAnomaly`를 실제로 호출하지 않습니다. 정상 판정 시 저장 생략, 현재 시도 거래 생성 전 집계, 재요청·동시 요청의 동일 판정 재사용을 애플리케이션에서 보장하는 연결이 남아 있습니다.

원본 [danjjak@a296758](https://github.com/team-bestfriend/danjjak/commit/a296758aa405a67343114c34e2f375c24adb63bd)의 `FdsEvaluator`, `AnomalyCommand`, `AnomalyRecord`와 현재 파일의 diff가 없음을 확인했습니다. #13 Mapper/XML, FE, 기존 Flyway와 build.gradle은 수정하지 않았습니다. 이번 변경은 재실행 가능한 검증과 근거 기록입니다.

## 실제 수행

환경: Windows, Eclipse Temurin JDK 17.0.18, Gradle 8.14.3, MySQL 8.4.11, Flyway 13.4.0. 사용자 개발 DB 대신 이번 검증에서 만든 임시 DB를 사용했습니다. Flyway V1~V9를 빈 DB에 모두 적용했습니다.

| 검증 | 근거·결과 |
| --- | --- |
| 금액·횟수·사유 | 기존 [FdsEvaluatorTest](../../backend/src/test/java/com/bestfriend/danjjak/transfer/service/FdsEvaluatorTest.java) 8건: 9,999,999/10,000,000/10,000,001원, 완료 0/1/2/3건, NORMAL/MEDIUM/HIGH, 두 사유와 불변 목록 |
| 시간 양끝 | [FdsPersistenceIntegrationTest](../../backend/src/test/java/com/bestfriend/danjjak/transfer/mapper/FdsPersistenceIntegrationTest.java): 판정 시각 기준 -601/-600/-599/0/+1초에서 각각 0/1/1/1/0건 |
| 사용자 전체 완료 집계 | 같은 사용자의 서로 다른 내 계좌에서 직접 1건·패턴 1건을 합산. 다른 사용자 및 DEPOSIT/WITHDRAWAL/PAYMENT 제외 |
| 완료가 아닌 기록 | FAILED/CANCELLED/STARTED 실행 및 취소·대기 판정만 있는 경우 완료 집계 0. 다른 세션에서 미커밋 거래가 보이지 않으며 롤백된 거래는 집계 0 |
| 동의·다른 속성 | 이용 기록 동의와 계좌번호 변경 전후 동일 결과. 판정은 금액과 완료 횟수의 두 사유만 사용 |
| 판정 저장 | 두 사유를 하나의 HIGH 행에 저장하고 생성 ID·사유·횟수 재조회. 차감·새 거래·최종 결정 없음. 타 사용자 조회 차단 |
| 판정 조회·제약 | 일치하는 미결정 시도 조회는 같은 ID. 금액·출금 계좌·사용자 변경 또는 취소 후 조회는 불일치. 패턴 실행의 두 번째 판정 INSERT는 DB 유일 제약으로 거절 |
| 정상 판정 | 정책 평가 자체에 저장 효과 없음. DB는 NORMAL 판정 행 INSERT를 거절. 실제 서비스의 정상 분기 저장 생략은 별도 미검증 |

테스트는 실제 `TransferMapper.xml`을 MyBatis로 로드합니다. JDBC는 테스트 자료 준비·독립 조회에만 사용하고, 집계와 판정 저장·조회는 운영 Mapper를 호출합니다. 자료는 각 테스트의 전용 사용자/계좌/패턴으로 만들고 모두 롤백합니다. fixture 변경 후 오래된 값을 읽지 않도록 검증 세션의 캐시 범위만 STATEMENT로 지정했습니다.

## 전체 빌드와 미검증 항목

일반 `gradle -p backend compileJava --console=plain`은 최신 main에서도 실패했습니다. #18의 `UserController`가 빈 `UserDtos`의 `AccessibilitySettings`, `ConsentSettings`, `ConsentUpdateRequest`, `CurrentUserResponse`를 참조하여 9개 컴파일 오류가 발생합니다. #16 변경 전부터 존재하는 문제입니다.

이를 숨기지 않고 [범위를 한정한 검증 스크립트](../../backend/verification/fds.init.gradle)로 FDS 정책·Mapper·모델과 해당 테스트만 별도로 컴파일했습니다. 스크립트는 `build/fds-verification`을 사용하고 기본 빌드 설정·운영 코드는 바꾸지 않습니다. **이 명령의 성공은 전체 앱 컴파일·기동·WAR 성공을 의미하지 않습니다.**

종료 전 남은 조건:

- 실제 직접/패턴 송금 경로가 같은 서버 판정을 호출하고, 현재 송금의 거래 INSERT보다 집계를 먼저 실행하는지 확인.
- 정상 시 anomaly 행 없음, 비정상 시 하나의 판정만 저장하는 실제 서비스 분기 확인.
- 직접 송금에는 패턴 실행 UNIQUE 제약이 적용되지 않으므로, 재요청·동시 요청의 판정 조회·잠금·재사용 흐름 확인. Mapper의 단일 조회 성공만으로 중복 삽입 방지를 입증할 수 없음.
- 실패/취소/대기 경로가 잘못된 완료 거래를 생성하지 않는지 실제 트랜잭션 흐름에서 확인.
- [SC-008/009](../specs/requirements/validation-scenarios.md)의 FDS 호출·재확인·계속/취소 연결 확인. 카톡·화면 전체 검증은 각 담당 이슈의 범위와 함께 구분.
- main의 선행 컴파일 오류 해결 후 전체 테스트·WAR·HTTP 통합 검증 재실행.

## 재실행

V1~V9가 적용된 별도의 폐기 가능한 검증 DB만 지정합니다. 환경변수를 생략하면 DB 테스트 13건은 건너뜁니다.

```powershell
$env:DANJJAK_DB_INTEGRATION_TEST = 'true'
$env:DANJJAK_DB_URL = 'jdbc:mysql://127.0.0.1:<검증포트>/danjjak?serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true'
$env:DANJJAK_DB_USERNAME = '<검증 DB 사용자>'
$env:DANJJAK_DB_PASSWORD = '<로컬 검증 비밀번호>'
gradle -p backend -I verification/fds.init.gradle test --console=plain
```

JUnit 결과는 `backend/build/fds-verification/test-results/test`, HTML 결과는 `backend/build/fds-verification/reports/tests/test`에 생성됩니다. 비밀번호·환경 파일·DB·빌드 결과는 커밋하지 않습니다.
