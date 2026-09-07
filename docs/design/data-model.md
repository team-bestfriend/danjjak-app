# 데이터 관계와 모델 책임

[아키텍처](../specs/requirements/architecture.md) · [백엔드 설계](backend-design.md) · [DB 계획](../../db/README.md)

MyBatis로 업무 단위의 저장·조회를 구성합니다. 아래 표는 개념 관계와 모델 책임이며, 물리 스키마는 담당 구현 이슈에서 확정합니다.

| 데이터 개념 | 모델·소유 패키지 | 관계·설계 의도 |
| --- | --- | --- |
| `users` | `user/model/UserSettingsRecord` | 카카오 연결·현재 접근성·동의 완료와 두 선택값 분리 |
| `guardian_contacts` | `support/model/GuardianContactRecord` | 사용자별 전화번호 하나, 카카오 수신자·승인 계정 아님 |
| `registered_persons` | `account/model/RegisteredPersonRecord`, `RegisteredPersonCommand` | 사람 식별과 복수 계좌 분리, 최초 계좌 포함 등록 |
| `bank_accounts` | `AccountRecord`, `RegisteredPersonAccountRecord`, `RecipientAccountCommand`, `OwnedAccountImportCommand` | 내/받는 계좌 분리, 사람별 여러 계좌, 모의 후보 중복 없는 추가 |
| `financial_patterns` | `pattern/model/PatternRecord`, `PatternCommand` | 한 활성 번호에 한 패턴, 특정 받는 계좌 연결, 비활성 이력 보존 |
| `pattern_steps` | `PatternStepRecord`, `StepCommand`, `GuidanceRecord` | 단계 식별/순서 구분, 시작 안내는 단계와 별도, 기본 문구·방식·파일 참조 |
| `pattern_executions` | `ExecutionCommand`, `PatternService`, `TransferService` | 동의한 패턴만 시작, 실제 선택 내 계좌·종료·결과 |
| `step_execution_logs` | `StepVisitRecord` | 재진입별 방문·누적 행동·실측 시간, 원시 클릭 없음 |
| `transactions` | `account/model/TransactionRecord`, `transfer/model/TransactionCommand` | 잔액과 함께 확정, 당시 받는 정보·결과 잔액 보존, 직접 송금은 패턴 연결 없음 |
| `anomaly_events` | `transfer/model/AnomalyRecord`, `AnomalyCommand`, `support/model/NotificationAnomalyRecord` | 이상 시도당 한 기록, 결정 전 거래 없음, 실제 알림 성공만 시각 |
| 조회 전용 투영 | `TransferAccountRecord`, `RecipientRecord`, `PatternUsageRecord`, `StepAnalysisRecord` | 기능별 조회 모델; 별도 테이블·ORM 엔티티로 간주하지 않음 |

| 경계 | 원칙 |
| --- | --- |
| 계좌 수·준비 | 모의 초기 자료 수를 계좌 상한으로 사용하지 않음. 재불러오기 시 잔액/거래/연결 보존 |
| 실행·금융 | 거래·차감·해당 마지막 방문·실행 결과의 소유권을 계약에서 정함. 종료 후 재송금·중복 종료 없음 |
| 분석 | 읽을 때 집계, 한국 날짜 기간·비활성 이력 유지. 미종료 실행 제외, 임의 종료 시각 없음 |
| 파일 | 바이너리는 파일 저장소, 업무 DB에는 대상·소유권·형식·참조·문구 불일치 |
| 민감값 | 원문 PIN·OAuth 토큰·명령 문자열·녹음 본문·실금융 자격정보를 업무 테이블/로그에 저장하지 않음 |
| 확장 | 필요한 저장 정보만 추가. 보고서/원시 이벤트/보호자 계정/알림 이력 테이블을 임의 도입하지 않음 |

- 첫 마이그레이션은 본선 이슈가 정의합니다. 적용된 버전은 불변이며 이후 변경은 새 버전으로 작성합니다.
- 상세 규칙: [사람·계좌](../specs/requirements/people-accounts.md), [음성](../specs/requirements/guidance-voice.md), [송금](../specs/requirements/mock-transfer.md), [분석](../specs/requirements/usage-analysis.md).
