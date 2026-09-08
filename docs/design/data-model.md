# 데이터 관계와 모델 책임

[아키텍처](../specs/requirements/architecture.md) · [백엔드 설계](backend-design.md) · [DB 계획](../../db/README.md)

MyBatis로 업무 단위의 저장·조회를 구성합니다. [#11 HTTP 계약](../../contracts/openapi.yaml)에 맞춰 아래 물리 모델을 구현 기준으로 확정합니다. DDL·Flyway·모의 초기 자료의 작성과 실행 검증은 [#13](https://github.com/team-bestfriend/danjjak-app/issues/13)에서 수행합니다.

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

## 공통 물리 규약

- MySQL 8.4/InnoDB/utf8mb4. 아래 `ID`는 양의 `BIGINT`, `UUID`는 `CHAR(36)` ASCII, `TIME`은 UTC `DATETIME(3)`, `MONEY`는 `DECIMAL(16,0)`이며 `0..9007199254740991` 검증을 적용합니다. HTTP의 ID는 십진 문자열로 직렬화합니다.
- 별도 nullable 표시가 없으면 NOT NULL. 각 테이블의 PK `id`와 `created_at TIME`을 기본으로 하되 방문은 `visit_id UUID`, 송금 시도는 `attempt_id UUID`를 PK로 사용합니다. `updated_at TIME`은 수정 가능한 업무 행에만 추가합니다. 테이블별 추가 컬럼과 제약은 아래를 따릅니다.
- `revision BIGINT`는 1부터 증가하고 UPDATE의 WHERE 조건에 expectedRevision을 포함합니다. 삭제는 RESTRICT이며 사용자·패턴·단계·거래 이력을 cascade로 제거하지 않습니다. API의 비활성화는 패턴 번호 해제입니다.
- `request_id UUID`와 `creation_hash CHAR(64)`는 생성 재시도에 필요한 테이블에만 둡니다. 해시는 정규화한 안전한 생성 입력에만 적용합니다. 사용자·테이블·request_id 유일성과 최초 해시는 이후 수정에도 유지합니다.
- 이 문서의 enum 표기는 논리 값 집합입니다. 구현은 `VARCHAR`+CHECK를 기본으로 하며 API와 같은 값 집합을 사용합니다. ID 참조는 PK 외에도 필요에 따라 `(user_id,id)` 복합 UNIQUE/FK를 사용해 소유 사용자 불일치를 막습니다.

## 사용자·계좌·연락처

| 테이블 | 추가 컬럼 | 제약·인덱스 | 소유 mapper |
| --- | --- | --- | --- |
| `users` | `kakao_subject VARCHAR(64) NULL`, `name VARCHAR(50)`, `consent_completed BOOLEAN`, `usage_recording BOOLEAN`, `guardian_sharing BOOLEAN`, `font_size VARCHAR(10)`, `speech_speed VARCHAR(10)`, `default_voice_mode VARCHAR(10)`, `primary_account_id ID NULL`, `pattern_order_revision BIGINT` | kakao_subject UNIQUE. 세 동의 값 독립. primary_account_id는 같은 사용자 OWNED·imported 계좌인지 트랜잭션 검증. 카카오 토큰 저장 금지 | UserMapper |
| `banks` | `bank_code VARCHAR(10)`, `name VARCHAR(50)`, `logo_url VARCHAR(500) NULL` | bank_code UNIQUE. 표시/입력 은행 목록의 기준 | AccountMapper |
| `registered_persons` | `user_id ID`, `name VARCHAR(50)`, `relationship VARCHAR(30)`, `revision BIGINT`, `request_id UUID`, `creation_hash CHAR(64)` | FK users; UNIQUE(user_id,id), UNIQUE(user_id,request_id). 최초 계좌와 같은 트랜잭션 | AccountMapper |
| `bank_accounts` | `user_id ID`, `kind VARCHAR(10)`, `registered_person_id ID NULL`, `bank_code VARCHAR(10)`, `normalized_number VARCHAR(20)`, `alias VARCHAR(50) NULL`, `balance MONEY NULL`, `mock_pin_hash VARCHAR(255) NULL`, `imported_at TIME NULL`, `revision BIGINT`, `request_id UUID NULL`, `creation_hash CHAR(64) NULL` | kind=OWNED/RECIPIENT. OWNED: person NULL, balance/PIN 비교값 존재. RECIPIENT: person 필수, balance/PIN/imported_at 모두 NULL. FK banks(bank_code), FK(user_id,person_id)→persons(user_id,id). UNIQUE(user_id,id), UNIQUE(person_id,bank_code,normalized_number), UNIQUE(user_id,request_id). 소유 계좌 후보와 불러온 계좌는 imported_at으로 구분 | AccountMapper |
| `guardian_contacts` | `user_id ID`, `phone_number VARCHAR(30)` | user_id UNIQUE, FK users. 사용자별 전화번호 하나 | SupportMapper |

은행 seed는 코드/표시명이며 로고는 선택입니다. 초기 OWNED 계좌의 해시만 `mock_pin_hash`에 저장하고 원문 PIN을 migration/seed에 넣지 않습니다. 받는 계좌에 가짜 잔액이나 PIN 컬럼 값을 채우지 않습니다. OWNED의 기본 여부는 users.primary_account_id 비교로 계산하고 별도 중복 플래그를 저장하지 않습니다. 사용자의 계좌 후보 재선택은 같은 행의 imported_at을 유지합니다.

## 패턴·단계·안내·가족 파일

| 테이블 | 추가 컬럼 | 제약·인덱스 | 소유 mapper |
| --- | --- | --- | --- |
| `pattern_templates` | `pattern_type VARCHAR(30)`, `title VARCHAR(50)`, `default_description VARCHAR(500)`, `available BOOLEAN` | pattern_type UNIQUE. OpenAPI의 7종만 제공 | PatternMapper |
| `template_steps` | `template_id ID`, `step_code VARCHAR(30)`, `step_order INT`, `title VARCHAR(50)`, `default_text VARCHAR(500)` | FK template; UNIQUE(template_id,step_order), UNIQUE(template_id,step_code). 등록 송금 6단계, 각 조회/전화 1단계 | PatternMapper |
| `financial_patterns` | `user_id ID`, `template_id ID`, `title VARCHAR(50)`, `recipient_account_id ID NULL`, `active BOOLEAN`, `shortcut_number TINYINT NULL`, `revision BIGINT`, `request_id UUID`, `creation_hash CHAR(64)` | FK user/template, FK(user_id,recipient_account_id)→accounts(user_id,id); 서비스가 RECIPIENT 종류 검증. TRANSFER는 계좌 필수, 나머지는 NULL. UNIQUE(user_id,shortcut_number), UNIQUE(user_id,id), UNIQUE(user_id,request_id). active=false이면 번호NULL, true이면1–12 | PatternMapper |
| `pattern_steps` | `pattern_id ID`, `template_step_id ID`, `step_code VARCHAR(30)`, `step_order INT`, `title VARCHAR(50)` | FK pattern/template_step. UNIQUE(pattern_id,id), UNIQUE(pattern_id,step_order), UNIQUE(pattern_id,template_step_id). 생성 시 템플릿을 복사, 자유 단계 변경 API 없음 | PatternMapper |
| `guidance_targets` | `user_id ID`, `pattern_id ID`, `kind VARCHAR(5)`, `step_id ID NULL`, `target_key VARCHAR(32)`, `default_text VARCHAR(500)`, `custom_text VARCHAR(500) NULL`, `voice_mode VARCHAR(10) NULL`, `revision BIGINT`, `text_revision BIGINT`, `recording_mismatch BOOLEAN`, `current_audio_id UUID NULL` | FK(user_id,pattern_id)→patterns(user_id,id); FK(pattern_id,step_id)→steps(pattern_id,id). START이면 step NULL·target_key='START'; STEP이면 step 필수·target_key='STEP:'+stepId. UNIQUE(pattern_id,target_key), UNIQUE(user_id,id). 실제 text는 COALESCE(custom_text,default_text) | GuidanceMapper |
| `voice_files` | `id UUID`, `user_id ID`, `target_id ID`, `upload_id UUID`, `storage_key VARCHAR(500)`, `mime_type VARCHAR(20)`, `size_bytes INT`, `duration_ms INT`, `file_sha256 CHAR(64)`, `recorded_text_revision BIGINT`, `state VARCHAR(20)` | FK(user_id,target_id)→targets(user_id,id); UNIQUE(user_id,upload_id), UNIQUE(target_id,id). state=ACTIVE/RETIRED. 1..10MiB, 1..90000ms. guidance_targets의 FK(id,current_audio_id)→files(target_id,id)는 테이블 생성 후 추가 | GuidanceMapper |

`voice_files.id`는 업로드 UUID와 같은 값으로 사용합니다. 순환 참조를 위해 guidance의 current_audio_id는 처음 NULL로 생성하고 파일 삽입 후 같은 트랜잭션에서 연결합니다. 파일 교체 후 RETIRED 메타데이터는 해당 사용자/대상 수명 동안 업로드 재시도를 식별하기 위해 유지하며 바이너리는 정리할 수 있습니다. 공개 재생은 현재 참조 파일만 허용합니다. 물리 경로는 API에 노출하지 않습니다.

패턴 description은 START 대상의 text 투영이며 별도 중복 컬럼을 두지 않습니다. 전역 음성 변경은 voice_mode가 NULL인 대상의 유효 방식만 바꿉니다. 저장 문구의 변경은 text_revision과 revision을 증가시키며 현재 녹음이 있으면 recording_mismatch를 true로 유지합니다.

## 실행·방문

| 테이블 | 추가 컬럼 | 제약·인덱스 | 소유 mapper |
| --- | --- | --- | --- |
| `pattern_executions` | `user_id ID`, `pattern_id ID`, `source_bank_account_id ID NULL`, `request_id UUID`, `status VARCHAR(10)`, `started_at TIME`, `ended_at TIME NULL`, `terminal_reason VARCHAR(30) NULL` | FK(user_id,pattern_id)→patterns, FK(user_id,source)→accounts. UNIQUE(user_id,id), UNIQUE(user_id,request_id). status=STARTED/COMPLETED/CANCELLED/FAILED. STARTED의 종료NULL, 나머지 종료필수. INDEX(user_id,ended_at,pattern_id) | PatternMapper |
| `step_execution_logs` | `visit_id UUID`, `execution_id ID`, `pattern_id ID`, `step_id ID`, `visit_number INT`, `entered_at TIME`, `left_at TIME NULL`, `retry_count INT`, `back_count INT`, `wrong_touch_count INT`, `route_deviation BOOLEAN` | FK execution, FK(pattern_id,step_id)→steps. 실행과 같은 pattern인지 서비스 및 복합 FK(execution_id,pattern_id)로 검증. UNIQUE(execution_id,visit_number), UNIQUE(execution_id,visit_id). left_at≥entered_at, counts≥0 | PatternMapper |

복합 방문 FK를 위해 executions에 UNIQUE(id,pattern_id)를 추가합니다. 한 실행의 진행 중 방문이 최대 하나이도록 실행 행 잠금 아래 이전 종료/다음 생성을 처리합니다. 누적값은 max/OR로 UPDATE합니다. 일반 방문 업데이트는 본문에 leftAt을 받지 않으며 다음 진입이나 업무 종료에서만 관측 종료 시각을 확정합니다. 종료불명 행에 배치 작업으로 종료 시각을 채우지 않습니다.

기록 거절 상태에서 execution/visit 행을 생성하지 않습니다. 금융 결과의 실제 선택 source ID는 확정 시 기록된 실행에 함께 반영합니다. 단순 화면 기본값을 실제 출금 계좌로 고정하지 않습니다. 실행/방문이 동의 철회 후 종료될 때 새로운 행동 합계는 추가하지 않습니다.

## 금융·FDS

| 테이블 | 추가 컬럼 | 제약·인덱스 | 소유 mapper |
| --- | --- | --- | --- |
| `transfer_attempts` | `attempt_id UUID`, `user_id ID`, `request_hash CHAR(64)`, `source_bank_account_id ID`, `recipient_name VARCHAR(50)`, `recipient_bank_code VARCHAR(10)`, `recipient_normalized_number VARCHAR(20)`, `recipient_account_id ID NULL`, `amount MONEY`, `execution_id ID NULL`, `status VARCHAR(20)`, `accepted_at TIME`, `resolved_at TIME NULL` | FK(user_id,source)→accounts, FK(user_id,recipient)→accounts, FK(user_id,execution)→executions. UNIQUE(user_id,attempt_id), UNIQUE(execution_id). status=REQUIRES_REVIEW/COMPLETED/CANCELLED. PIN・행동 원문・기록 없는 pattern ID 저장 없음 | TransferMapper |
| `transactions` | `user_id ID`, `account_id ID`, `attempt_id UUID NULL`, `execution_id ID NULL`, `direction VARCHAR(10)`, `category VARCHAR(20)`, `amount MONEY`, `counterparty_name VARCHAR(50)`, `counterparty_bank_code VARCHAR(10) NULL`, `counterparty_normalized_number VARCHAR(20) NULL`, `balance_after MONEY`, `occurred_at TIME` | FK(user_id,account)→accounts, FK(user_id,attempt)→attempts, FK(user_id,execution)→executions. UNIQUE(attempt_id), UNIQUE(execution_id). 기존 모의 입출금은 attempt/execution NULL. INDEX(user_id,direction,category,occurred_at), INDEX(account_id,occurred_at,id) | TransferMapper 쓰기 / AccountMapper 조회 |
| `anomaly_events` | `user_id ID`, `attempt_id UUID`, `risk_level VARCHAR(10)`, `high_amount BOOLEAN`, `repeated_transfer BOOLEAN`, `recent_transfer_count INT`, `assessed_at TIME`, `rechecked_at TIME NULL`, `decision VARCHAR(10) NULL`, `resolved_at TIME NULL`, `transaction_id ID NULL`, `actual_notified_at TIME NULL` | FK(user_id,attempt)→attempts; UNIQUE(attempt_id). MEDIUM은 사유1개, HIGH는2개. decision=NULL/CONTINUE/CANCEL. 계속은 transaction 필수, 미결정/취소는NULL. 실제 전송 성공 시각만 저장 | TransferMapper / SupportMapper 알림 투영 |

`transfer_attempts`는 새 집계/로그 테이블이 아니라, 응답 유실 재조회 및 경고 대기 중 불변 금융 스냅샷을 위한 저장 대상입니다. 기존 transactions는 완료 전 시도를 표현할 수 없고 anomaly_events는 정상 시도에 생성할 수 없어 이 행이 필요합니다. PIN 검증 실패는 행을 만들지 않습니다. 실제 요청이 커밋되지 않은 상태는 GET의404로만 나타내며 완료로 추정하지 않습니다.

거래·시도·실행의 사용자/출금 ID 및 판정의 거래 연결 일치는 TransferService 트랜잭션에서 검증합니다. FK용 transactions의 UNIQUE(user_id,id)를 추가합니다. 같은 사용자의 여러 내 계좌 송금은 users 행 잠금으로 직렬화하여 FDS 시간창 평가와 차감 순서를 일치시킵니다. 기존 인덱스에서 해당 사용자의 완료 출금 TRANSFER만 집계합니다. 정상은 anomaly 행이 없고, 경고는 유효 제출 한 건당 하나입니다.

## 관계 구현·검증 담당

| 후속 이슈 | 이 모델을 사용하는 범위 | 필수 확인 |
| --- | --- | --- |
| #12 | 환경·MyBatis/Flyway 실행 준비 | 외부 환경값, DB 실패 구분 |
| #13 | 모든 위 테이블·인덱스·FK, 초기 모의 자료 | 빈 DB 마이그레이션, 재실행 중복/초기화 없음, 8개 기본 패턴·7종 템플릿·모든 기본 안내 |
| #17–21 | 사용자·설정·계좌·연락처 | 같은 카카오 사용자, 독립 동의, 복수 계좌, 실사용 출금 선택 |
| #22–26 | 패턴·안내·실행·방문 | 번호 교환 원자성, 대상 식별, 동의·재진입·누적합 |
| #16/#28/#37 | 시도·판정·금융 확정 | 사용자 단위 반복 판정, 중복 제출/결정, 금융/방문/실행 동시 확정 |
| #25/#30/#38 | 문구·가족 파일·분석 적용 | 부분 저장 재시도, 교체 실패 보존, 불일치 유지, 오래된 비교 차단 |
| #29/#39 | 분석·카카오 시연 | 조회 집계, 미종료 제외, 모의 전송의 영속 성공 이력 없음 |

SQL·마이그레이션 실행·성능·외부 제공사 검증은 아직 수행하지 않았습니다. API 계약 정적 검증 결과는 [#11 검증 기록](../../contracts/validation.md)에 남깁니다.
