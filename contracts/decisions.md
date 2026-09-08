# HTTP·저장 책임 결정

[#11](https://github.com/team-bestfriend/danjjak-app/issues/11)의 구현 기준입니다. 경로·필드·상태 코드는 [OpenAPI](openapi.yaml), 업무 규칙은 [기능 명세](../docs/specs/requirements.md), 물리 컬럼·제약은 [데이터 모델](../docs/design/data-model.md)이 소유합니다. 이 문서는 경계에서의 처리 순서와 재시도 책임을 정합니다.

## 공통 전송·인증

- API 경로는 `/api`로 시작합니다. Tomcat context `/danjjak` 배포 시 실제 경로는 `/danjjak/api/...`이며 FE의 `VITE_API_BASE_URL=/danjjak` 또는 같은 경로를 연결한 개발 프록시를 사용합니다. 개발 origin·쿠키 경로는 #12에서 실행 환경에 맞게 설정합니다.
- 성공은 명시된 JSON 객체/배열 자체입니다. 공통 `{data: ...}` 포장은 추가하지 않습니다. `204`는 본문이 없고 TTS/파일 성공은 바이너리입니다. 오류는 모든 경로에서 `{code, message, fieldErrors, retryable, requestId}`입니다.
- `requestId` 오류 응답 값은 서버 추적 ID이며 요청 본문의 생성 중복 방지 `requestId`와 역할이 다릅니다. 로그에는 추적 ID·경로 템플릿·오류 코드만 남기고 민감 입력을 담지 않습니다.
- 세션은 `JSESSIONID`, 클라이언트는 `credentials: 'include'`. 인증되지 않은 세션 확인만 `200 authenticated=false`이고 보호 API는 `401 SESSION_REQUIRED`입니다. 이름·설정·동의·계좌 준비 상태는 `CurrentUser` 하나로 공유합니다.
- 상태 변경 및 합성 요청은 세션 조회의 `csrfToken`을 `X-CSRF-Token`으로 전달합니다. OAuth 시작/콜백은 브라우저 이동이며 세션에 보관한 일회 `state`를 검증합니다. 임의 리다이렉트 URL은 받지 않습니다. 카카오 토큰은 서버 세션/자격정보 저장소에만 유지합니다.
- 인증 성공 때 세션 ID와 CSRF 값을 회전합니다. 운영 쿠키는 HttpOnly·Secure·SameSite=Lax이며 FE와 API는 같은 site 배포를 기본으로 합니다. CORS가 필요하면 허용 origin을 명시하고 credentials를 사용합니다.
- 모든 사용자별 조회·변경은 세션 소유권을 먼저 확인합니다. 다른 사용자의 ID는 없는 대상과 동일한 `404`로 응답합니다. 다른 사용자 데이터·전체 계좌번호·PIN·토큰·SQL 예외를 오류 본문에 넣지 않습니다. 응답은 `Cache-Control: no-store`입니다.
- DB ID는 양의 BIGINT를 표현하는 **문자열**이며 FE는 `Number()`나 `Number.isInteger(id)`를 사용하지 않습니다. UUID는 요청/업로드/송금 시도/방문에만 사용합니다. 금액은 정수 원이며 JSON 정확 범위를 초과하는 값은 형식 오류입니다. 송금 한도 정책을 새로 추가한 것이 아닙니다.
- 타임스탬프는 UTC RFC 3339, DB는 UTC `DATETIME(3)`, 분석 날짜는 `Asia/Seoul`입니다. 문자열 길이는 Unicode 코드 포인트 기준입니다.

## 식별자와 수정 범위

| 식별자 | 의미·검증 | 소유자 |
| --- | --- | --- |
| `registeredPersonId` | 사용자에게 등록된 사람. 계좌 ID로 대체하지 않음 | AccountService |
| `accountId`, `sourceBankAccountId` | 같은 `bank_accounts.id` 중 본인·OWNED·불러오기 완료 계좌 | AccountService / TransferService |
| `recipientAccountId` | 같은 테이블 중 RECIPIENT 계좌. 한 사람에게 여러 ID | AccountService |
| `patternId` | 저장된 업무. `shortcutNumber`와 별도, 비활성 이력 보존 | PatternService |
| `stepId` | 패턴에 속한 실제 단계. 번호·순번·`stepCode`로 추정하지 않음 | PatternService |
| `targetId` | 실제 안내 대상. START는 `stepId=null`, STEP은 실제 단계 하나 | GuidanceService |
| `executionId` | 기록 동의 후 시작된 패턴 실행. 동의 거절/홈 직접 송금에는 없음 | PatternService |
| `visitId` | 한 번의 단계 진입 UUID. 재진입마다 새 값 | PatternService |
| `attemptId` | 한 금융 시도의 UUID. 직접/패턴 공통, 응답 유실 시 재사용 | TransferService |
| `anomalyEventId` | 경고가 발생한 시도당 하나. 재확인·알림·결정이 공유 | TransferService |

받는 계좌 수정은 같은 계좌 ID를 유지합니다. 다른 계좌나 패턴 연결을 바꾸지 않고 과거 거래의 당시 정보도 변경하지 않습니다. 이번 송금에서 선택한 다른 출금/받는 계좌는 저장 기본 계좌나 패턴 연결에 반영하지 않습니다. 계좌 삭제·사람 삭제 API는 범위에 없습니다.

전체 번호는 받는 계좌의 `/edit` 조회와 명시적 등록·수정 입력에만 사용합니다. 목록/상세/송금/거래 응답은 `maskedAccountNumber`입니다. 직접 송금의 미등록 번호는 요청에서 받되 등록 사람/계좌로 자동 저장하지 않습니다.

## 생성·수정·부분 저장 재시도

### 생성

- 사람+첫 계좌, 받는 계좌 추가, 패턴 생성은 요청 UUID와 안전한 최초 입력의 SHA-256을 각 업무 행에 저장합니다. 유일 키는 사용자+작업 종류에 해당하는 테이블+UUID입니다. 같은 키·같은 입력은 같은 생성 ID, 같은 키·다른 입력은 `409 REQUEST_ID_CONFLICT`입니다. UUID는 관련 데이터 수명 동안 재사용되지 않습니다.
- `requestId`를 포함한 원본 JSON을 범용 로그/테이블에 보관하지 않습니다. 생성 해시에 PIN·카카오 토큰·음성 바이너리·인식 문자열이 포함되는 작업을 만들지 않습니다.
- 계좌 불러오기는 후보의 기존 `accountId`와 `imported_at`으로 중복을 방지합니다. 선택 계좌 전체를 하나의 트랜잭션으로 처리하고 잔액·거래·기존 기본 계좌를 보존합니다.
- 패턴 생성은 패턴+저장 단계+START/각 STEP의 유효 기본 안내까지 원자적으로 완료합니다. 생성 응답을 잃으면 **같은 requestId로 생성 요청만 재확인**하고 반환 `patternId`를 유지합니다.

### 수정과 번호 목록

- 사람·계좌·패턴·안내에는 `revision`이 있습니다. 수정 요청은 `expectedRevision`을 요구하고 원자적 비교 후 변경합니다. `409 STALE_REVISION`이면 최신 값을 조회해 비교하며 미저장 초안을 자동 폐기하지 않습니다.
- 응답 유실 후 같은 수정 요청이 충돌하면 재조회하여 저장 여부를 확인합니다. 같은 값을 이미 저장한 사실을 확인한 경우에만 FE가 그 부분을 저장 완료로 표시합니다. 새 revision을 자동 끼워 넣어 타인의 수정을 덮어쓰지 않습니다.
- 활성 번호 목록에는 사용자별 별도 revision이 있습니다. 생성·비활성화·번호 이동이 모두 이를 증가시킵니다. 전체 활성 ID/번호 목록 검증과 저장을 한 트랜잭션으로 처리합니다. 번호 교환 시 `swapConfirmed=true`가 필요합니다. 번호 유일 제약을 유지하도록 같은 트랜잭션에서 해당 번호를 잠시 NULL로 해제한 후 배정합니다. 사용자 행을 잠가 외부에 중간 상태가 보이지 않게 합니다.

### 안내·녹음

1. 새 패턴 최종 저장 시 패턴 핵심 정보를 먼저 생성해 실제 단계와 안내 대상 ID를 얻습니다. 등록 초안의 `templateStepId`를 반환된 실제 단계에 매핑합니다.
2. 사용자가 명시적으로 바꾼 각 안내 대상만 `PUT guidance/{targetId}`로 저장합니다. `text=null`은 기본 문구 복원, `voiceMode=null`은 사용자 기본값 상속, **필드 생략은 기존 값 보존**입니다.
3. 녹음이 있다면 문구 저장 응답의 revision과 대본을 포함해 해당 대상의 `/audio`에 업로드합니다. `uploadId`는 파일 시도마다 하나이며 재시도에는 유지합니다. 문구·방식 저장과 녹음 업로드는 별도 확정 단위입니다.
4. FE는 `patternId`, 성공한 `targetId`/revision, 성공한 `uploadId`를 보존합니다. 녹음 실패 시 “문구 저장 완료·녹음 미저장”을 표시하고 **같은 패턴/대상의 실패 부분만** 다시 저장합니다. 이미 생성된 패턴을 새로 만들지 않습니다.
5. 서버는 임시 파일의 실제 MIME·크기·길이를 검사하고 `recordedText`가 현재 대본과 일치하는지 확인합니다. DB 참조 커밋 후 이전 파일을 정리합니다. DB 실패 시 새 임시 파일을 정리하고 이전 참조를 보존합니다.
6. 저장된 녹음의 문구가 바뀌면 `recordingMismatch=true`를 유지합니다. 같은 문구로 다시 되돌리거나 AI로 변경해도 자동 해제/삭제하지 않습니다. 성공한 가족 녹음 교체만 불일치를 해제합니다. 미저장 녹음의 대본이 달라졌다면 재녹음/초안 버리기 후 제출합니다.

업로드 중복 확인은 revision 확인보다 먼저 합니다. 같은 사용자·대상·uploadId·파일 SHA-256의 이미 커밋된 요청은 현재 대상 저장값을 반환합니다. 다른 대상/파일에 키를 재사용하면 충돌입니다. 과거 파일 교체를 다시 재생성하지 않습니다. 임시/정리 파일의 작업 디렉터리는 업무 데이터와 분리하며 바이너리는 DB에 저장하지 않습니다.

## 기록 동의와 종료 책임

| 상황 | 처리 |
| --- | --- |
| 동의 미완료 | 업무 시작은 `409 CONSENT_NOT_COMPLETED`, 두 동의 선택 화면으로 복귀 |
| 기록 거절 후 패턴 시작 | `loggingEnabled=false, execution=null`; 실행·방문·행동 요청 없음 |
| 동의 후 시작 | 같은 시작 UUID 재시도는 같은 실행. 초기 `sourceBankAccountId=null` |
| 실행 도중 기록 철회 | 새 방문/행동은 `403 USAGE_CONSENT_REQUIRED`; 기존 실행은 행동 없는 종료 정리만 |
| 동의 거절 시작 후 동의 변경 | 이미 시작한 업무에 기록을 소급 생성하지 않음. 다음 명시적 시작부터 기록 |
| 새로고침·강제 종료 | 종료 시각을 만들지 않음. 진행 중 실행은 분석 제외 |

`VisitStart`의 관측 `enteredAt`, 이전 방문의 `leftAt`, 종료 시 `occurredAt`은 FE가 행동 시점에 생성합니다. 서버는 실행 시작 이후·해당 방문 시작 이후·요청 수신 시각에서 허용 시계 오차 5초 이내인지 검사합니다. 허용 오차를 벗어난 값은 `422 INVALID_EVENT_TIME`이며 서버 현재 시각으로 조작해 저장하지 않습니다. 동일 종료 재시도는 최초 저장 시각을 유지합니다. 송금 확정·판정 시각은 서버 시계입니다.

누적 행동은 `max`/불리언 OR로 병합합니다. AMOUNT/PIN 단계의 `wrongTouchCount`는 0이어야 합니다. 종료된 실행/방문을 수정하는 요청은 거절합니다. 동의 철회와 최종 확정은 같은 사용자 잠금 아래 순서를 확정하여 철회 후 행동이 추가되지 않게 합니다.

| 종료 사건 | 진입 API·소유자 | 원자적 변경 |
| --- | --- | --- |
| 정상 송금 | `createTransfer` → TransferService | 차감·거래·시도 결과·마지막 방문·실행 완료 |
| 경고 후 계속 | `resolveAnomaly` → TransferService | 잔액 재검사·차감·거래·판정·방문·실행 완료 |
| 경고 취소 | `resolveAnomaly` → TransferService | 차감 없이 판정·시도 취소·방문·실행 취소 |
| 조회 성공/정상 빈 결과 | `completeInquiry` → PatternService→AccountService | 실제 조회 성공 확인·선택 내 계좌·방문·실행 완료 |
| 고객센터 전화 버튼 | `completeSupportCall` → PatternService | 해당 전화 선택 시각·방문·실행 완료; 통화 성공 의미 없음 |
| 금융 시도 전 사용자 취소/복구 불가 실패 | `finishExecution` → PatternService | 마지막 방문·실행 CANCELLED/FAILED |
| 수정 가능한 금액/PIN 오류 | 금융 미확정, 실행 유지 | 동의 시 전달된 누적값만 갱신; 종료 시각 없음 |

조회 기록 동의 유지 시 `completeInquiry`가 첫 결과 조회와 완료를 함께 처리합니다. 완료 이후 같은 결과 화면에서 계좌/필터를 바꾸면 일반 조회 API만 사용하며 새 방문/완료를 만들지 않습니다. 조회 도중 동의가 철회되면 일반 조회로 전환하고 기존 실행은 `finishExecution`에 행동 없는 CANCELLED 정리 요청을 보냅니다. 이는 금융 조회 실패가 아니라 기록 세션의 취소입니다.

FE는 송금 결과의 `executionId`를 참고할 수 있지만 금융 확정 응답 이후 별도 `finishExecution`/`updateVisit`를 보내지 않습니다. 직접 송금은 `context.type=DIRECT`, 기록 없는 패턴은 `PATTERN_UNRECORDED`, 기록 중 패턴은 `PATTERN_RECORDED`입니다. 패턴 송금은 REGISTERED 받는 계좌만 허용합니다. 현재 명시적으로 선택한 다른 받는 계좌는 허용하되 저장 패턴에 반영하지 않습니다.

## 송금 시도·FDS·불확실한 응답

1. FE는 최종 제출 전 한 `attemptId`를 생성합니다. 네트워크 유실/같은 입력 재시도/PIN 수정은 이 값을 유지합니다. 금액·출금·받는 계좌를 바꾸면 이전 결과를 먼저 확인하고 미결정 경고를 취소한 뒤 새 시도를 시작합니다.
2. 서버는 사용자 행을 잠가 같은 사용자의 여러 출금 계좌 송금을 직렬화합니다. 중복 시도 조회→소유·준비·입력·PIN·잔액 검증→반복 판정→금융/기록 확정을 같은 업무 경계에서 처리합니다. 업무 잠금 순서는 사용자→내 계좌→시도/판정→실행/방문입니다.
3. 잘못된 PIN/입력은 영속 금융 시도를 만들지 않습니다. 기록 동의가 유지되는 유효 실행의 누적 행동만 별도 트랜잭션으로 반영할 수 있습니다. 금융 예외로 함께 롤백하거나 금융 성공을 가장하지 않습니다. PIN 원문/해시는 송금 시도에 저장하지 않습니다.
4. 유효 제출의 금융 의미(출금 ID·받는 계좌 ID 또는 직접 입력 정규화 값·금액·기록된 실행 ID)를 비교용 해시로 저장합니다. 등록 계좌의 이름/번호는 최초 접수 스냅샷을 사용하며 재요청 시 수정된 계좌를 다시 해석해 기존 결과를 바꾸지 않습니다. PIN·행동 합계는 해시에 넣지 않습니다. 재시도는 저장 결과를 먼저 반환하고 금융 확정 이후 행동을 다시 반영하지 않습니다. 이미 경고 대기면 누적값은 방문 API 또는 최종 결정 요청으로 전달합니다.
5. FDS 판정 값과 조건은 [FDS 명세](../docs/specs/requirements/fds-guardian.md)가 소유합니다. 정상은 판정 행 없음, 경고는 한 시도에 한 행입니다. 경고의 계좌·금액·이름은 당시 스냅샷으로 유지합니다. 다시 확인은 같은 스냅샷을 읽고 `recheckedAt`만 처음 기록합니다.
6. 계속/취소는 같은 사용자 잠금과 트랜잭션으로 처리합니다. 이미 해결된 판정에는 반대 결정을 보내도 저장된 결과를 반환하며 두 번째 차감은 없습니다. 계속 시 잔액 부족이면 422·미결정 유지입니다. 패턴 기록 동의가 유지되면 현재 마지막 방문 합계를 함께 제출하고 철회 후에는 null을 보냅니다.
7. 응답을 잃으면 `GET /api/transfers/{attemptId}`로 확인합니다. 404/통신 실패는 성공 또는 실패의 확정 증거가 아닙니다. 자동으로 다른 시도 ID를 만들어 보내지 않습니다. 사용자 재시도는 같은 ID·같은 금융 정보이며 PIN을 저장소에서 복원하지 않습니다.

송금 숫자와 `amountInWords`는 같은 정수의 한글 표현이어야 합니다. 비교 시 한글 표현의 공백만 정규화하고 단위는 유지합니다. 예시는 [FR-059](../docs/specs/requirements/mock-transfer.md)를 따릅니다. 조회/기록 실패가 확정 금융 작업의 재실행을 유발해서는 안 됩니다.

## 카카오 알림·음성·분석

- 카카오 알림은 같은 `anomalyEventId`의 미결정 HIGH 상태·동의·본인 소유·`confirmedSelfDemo=true`를 서버에서 검사합니다. 실제 수신자는 로그인 본인입니다. 모의 결과는 성공 실제 시각/별도 영속 알림 이력을 만들지 않습니다.
- 실제 알림 성공 결과는 판정 행에서 재사용합니다. 같은 사용자/판정의 알림 요청을 직렬화하고 외부 호출에 제한 시간을 둡니다. 시간 초과로 실제 전송 여부가 불명확하면 `502 NOTIFICATION_RESULT_UNKNOWN`으로 표시하고 자동 재전송하지 않습니다. 명확한 제공사 실패만 `MOCK_AFTER_FAILURE`로 전환합니다. 외부 제공사의 정확히 한 번 전송은 보장하지 않으며 장애 중 중복 방지 보완은 #39에서 검증합니다.
- AI 합성은 현재 문구/초안의 응답 바이너리를 반환합니다. 실패는 JSON 오류이며 FE는 질문·입력·주 행동을 유지합니다. 명령 인식 원음/문자열 업로드 API는 만들지 않습니다. 가족 녹음 본문은 명시적 가족 녹음 업로드에만 해당합니다.
- 분석은 실시간 읽기 집계이며 집계 테이블을 만들지 않습니다. 기간·정렬·동률·미종료 방문의 시간 제외는 [분석 명세](../docs/specs/requirements/usage-analysis.md)를 따릅니다. 기간 양 끝과 선택 후보를 비교 응답에 포함합니다.
- 제안 적용은 `from/to`, `expectedText`, `suggestedText`, `expectedRevision` 모두 검증합니다. 서버 재계산 제안과 다르면 저장하지 않습니다. 적용은 GuidanceService의 동일 안내 대상 저장 경로를 사용하여 녹음 불일치 처리와 재녹음 대상 ID를 보존합니다.
- FR-061/D-03의 사전 생성·캐시는 선택 #35로 남깁니다. D-01/02/04/05의 화면·표현 선택 상태를 HTTP 계약이 확정한 것으로 해석하지 않습니다.

## 오류 코드

아래는 구현 시 사용할 안정된 코드입니다. `message`는 상황에 맞는 쉬운 한국어, `fieldErrors`는 필드명/오류코드/문구만 제공합니다. `retryable=true`도 자동 송금/알림 재전송 허가는 아닙니다.

| HTTP | 코드 | 사용 범위 |
| --- | --- | --- |
| 400 | `INVALID_REQUEST`, `INVALID_DATE_RANGE`, `INVALID_OAUTH_STATE` | 모든 입력, 분석 날짜, 카카오 콜백 |
| 401 | `SESSION_REQUIRED` | 보호 API |
| 403 | `CSRF_INVALID`, `USAGE_CONSENT_REQUIRED`, `GUARDIAN_SHARE_CONSENT_REQUIRED` | 상태 변경, 방문/제안, 알림 |
| 404 | `NOT_FOUND`, `SOURCE_ACCOUNT_NOT_FOUND`, `RECIPIENT_ACCOUNT_NOT_FOUND`, `REGISTERED_PERSON_NOT_FOUND`, `TRANSFER_RESULT_NOT_FOUND` | 대상별 조회/저장, 송금 결과 확인 |
| 409 | `STALE_REVISION`, `REQUEST_ID_CONFLICT`, `ACCOUNT_ALREADY_EXISTS`, `PATTERN_NUMBER_CONFLICT`, `PATTERN_LIMIT_EXCEEDED`, `PATTERN_INACTIVE`, `CONSENT_NOT_COMPLETED`, `ACCOUNT_NOT_READY`, `EXECUTION_ENDED`, `VISIT_ENDED`, `TRANSFER_PENDING`, `ANOMALY_NOT_ELIGIBLE`, `RECORDING_TEXT_CHANGED`, `SUGGESTION_CHANGED`, `SUGGESTION_IDENTICAL` | 각 저장/시작/종료 경계 |
| 413 | `AUDIO_TOO_LARGE` | 가족 업로드 |
| 415 | `UNSUPPORTED_AUDIO_TYPE` | 가족 업로드 |
| 422 | `PIN_MISMATCH`, `INSUFFICIENT_BALANCE`, `AMOUNT_TEXT_MISMATCH`, `INVALID_RECIPIENT`, `INVALID_TEMPLATE`, `INVALID_EVENT_TIME`, `INVALID_ACTION_COUNTS`, `AUDIO_DURATION_EXCEEDED`, `EMPTY_AUDIO`, `SWAP_CONFIRMATION_REQUIRED` | 업무 검증 실패 |
| 500 | `INTERNAL_ERROR` | 내부 실패; 상세 원인·민감값 비공개 |
| 502 | `KAKAO_AUTH_FAILED`, `TTS_PROVIDER_FAILED`, `NOTIFICATION_RESULT_UNKNOWN` | 외부 연동 실패 |
| 503 | `DATABASE_UNAVAILABLE`, `DATABASE_NOT_CONFIGURED`, `KAKAO_NOT_CONFIGURED`, `DEMO_USER_CAPACITY_EXCEEDED`, `TTS_NOT_CONFIGURED`, `FILE_STORE_UNAVAILABLE`, `SUPPORT_NOT_CONFIGURED` | 실행/자료/제공사 준비 필요 |

동일 대상의 수정 충돌·세션 만료·계좌 준비 전 상태는 저장 성공으로 반환하지 않습니다. API별 적용 상태는 OpenAPI의 responses를 따릅니다.
