# 모의 송금

[목차](../requirements.md)

## 요구사항

| ID | 기능 | 완료 조건 |
| --- | --- | --- |
| FR-028 | 송금 정보 | 내 계좌·받는 계좌·금액 선택 후 전체 확인 |
| FR-029 | 직접 송금 | 미등록 이름·은행·계좌번호를 단계별 입력 |
| FR-030 | 본인 확인 | 선택한 내 계좌의 모의 비밀번호 4자리 확인 |
| FR-031 | 모의 처리 | 잔액 차감·거래 생성을 함께 성공 또는 함께 취소 |
| FR-059 | 금액 표시 | 쉼표·원 단위, 입력·확인·경고·완료에 한글 병기 |

## 등록된 사람 송금

| 순서 | 화면 | 질문·행동 |
| --- | --- | --- |
| 1 | 보낼 계좌 | ‘어느 내 계좌에서 보낼까요?’ → 다음 |
| 2 | 받는 사람 | ‘누구에게 보낼까요?’ → 다음 |
| 3 | 받는 계좌 | 선택한 사람의 계좌 확인 → 다음 |
| 4 | 보낼 금액 | ‘얼마를 보낼까요?’ → 다음 |
| 5 | 최종 확인 | ‘이대로 보낼까요?’ → ‘50,000원 보내기’ |
| 6 | 본인 확인 | 계좌 비밀번호 4자리 → 본인 확인 후 보내기 |

- 상단: ‘송금 1단계 · 보낼 계좌’처럼 순번·이름 병기.
- 기본·연결 계좌가 있어도 확인 단계 유지. 계좌 한 개도 동일.
- 최종 확인: 내 계좌·받는 사람·은행/계좌·금액·수수료 0원.
- 금액 포함 버튼은 본인 확인으로 이동. ‘계좌 비밀번호를 확인한 뒤 보내요.’ 안내.
- 실제 송금 요청은 비밀번호 입력 후 최종 제출 시 수행.

## 직접 입력: D-05 기본안

**내 계좌 → 이름 → 은행 → 계좌번호 → 받는 계좌 확인 → 금액 → 최종 확인 → 본인 확인**

| 항목 | 규칙 |
| --- | --- |
| 진입 | 홈 직접 송금 → 등록된 사람 또는 직접 입력 |
| 단계 | 입력 화면 분리·연속 번호 필수, 상세 순서는 D-05 |
| 이름 | 유효한 비공백 값, 최대 50자 |
| 은행 | 목록 선택, 코드 직접 입력 없음 |
| 계좌번호 | [공통 계좌 형식](people-accounts.md) |
| 확인 | 이름·은행·계좌번호 함께 표시 |
| 저장 | 연락처 자동 등록 없음 |
| 분석 | 홈 직접 송금은 패턴 이용 횟수 제외 |
| 반복 판정 | 완료된 직접 송금 거래도 포함 |

## 금액

| 숫자 | 표시 | 한글 |
| --- | --- | --- |
| 15000 | 15,000원 | 만 오천원 |
| 50000 | 50,000원 | 오만원 |
| 100000 | 100,000원 | 십만원 |
| 1000000 | 1,000,000원 | 백만원 |
| 10000000 | 10,000,000원 | 천만원 |
| 100000000 | 100,000,000원 | 일억원 |

- 0보다 큰 정수 원 단위. 빈 값·음수·소수·비숫자 제출 불가.
- 쉼표는 표시용. 숫자와 한글은 같은 금액으로 즉시 갱신.
- 잔액 초과: ‘잔액이 부족해요. 보낼 금액을 확인해 주세요.’
- 목록·조회: 쉼표·원 단위. 송금 결정 화면: 한글 병기.

## 결과·오류

| 결과 | 화면 | 데이터 |
| --- | --- | --- |
| 완료 | ‘김민수님에게 50,000원을 보냈어요.’, 한글 금액·잔액·홈으로 가기 | 차감 + 거래 한 건 |
| 추가 확인 | [이상거래 화면](fds-guardian.md) | 결정 전 차감·완료 거래 없음 |
| 비밀번호 오류 | ‘비밀번호가 맞지 않아요. 다시 입력해 주세요.’ 화면·음성 안내 | 비밀번호만 비움, 안전한 입력 유지 |
| 잔액 부족 | 금액 수정 | 차감·거래 없음 |
| 연결 실패·결과 미확인 | 확인 불가 안내 | 성공 추정·자동 재송금 없음 |
| 진행 정보 소실 | 초기화 이유·처음부터 진행 | 비밀번호 복원 없음 |

## Agent Notes

- Validate source ownership, PIN, balance, and FDS on the server at submission; recheck balance when continuing an anomaly.
- Keep selected source/recipient consistent across execution, submission, review, and result. Runtime choices must not alter saved defaults or pattern links.
- Treat name/account validation as format checking, not bank ownership verification.
- Mask PIN input; exclude it from storage, logs, analytics, and speech. Keep financial actions usable when audio fails.
- Reject inconsistent numeric/word amounts; never interpret 100000000 as 십만원.
- Clear prior transient state on completion, cancellation, and a new transfer. A result URL without verified data must not fabricate success.
- Persist debit and transaction atomically. Coordinate the last visit and execution exactly once using [logging boundaries](usage-analysis.md).
- A post-completion query/logging error must not trigger another transfer.
- Acceptance: [SC-005–009, 015, 017–018](validation-scenarios.md).
