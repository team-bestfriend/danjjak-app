# 이용 분석

[목차](../requirements.md)

**목적 기본안: 자주 쓰는 업무·안내 검토 단계를 확인하고 문구·가족 음성 개선.** 목적·배치는 D-02 검토 대상.

## 요구사항

| ID | 기능 | 완료 조건 |
| --- | --- | --- |
| FR-044 | 실행 기록 | 동의한 패턴 실행의 시작·종료·결과 |
| FR-045 | 단계 방문 | 재진입마다 별도 방문 |
| FR-046 | 행동 합계 | 재시도·뒤로·잘못 누름·경로 이탈 |
| FR-047 | 시간 | 방문 시작·종료 시각으로 계산 |
| FR-048 | 이용 횟수 | 패턴별 완료 횟수·전체 합계, 많이 쓴 순 |
| FR-049 | 안내 검토 단계 | 정해진 규칙으로 선정 |
| FR-050 | 문구 제안 | 단계별 쉬운 문구 템플릿 |
| FR-051 | 비교·적용 | 현재·제안 비교 후 명시적 저장 |
| FR-052 | 재녹음 | 변경한 같은 단계의 가족 녹음으로 연결 |

## 기록 상태

| 상태 | 기록·화면 |
| --- | --- |
| 동의 미완료 | 선택 완료 안내 |
| 기록 거절 | 새 실행·방문·행동 수집 없음, 설정 안내 |
| 동의·기록 없음 | 실제 자료 없음 |
| 동의·기록 있음 | 해당 사용자 자료 |
| 직접 송금 | 금융 거래는 유지, 패턴 분석 제외 |
| 확인창·편집·녹음 | 금융 실행 기록 아님 |

| 실행 상태 | 종료 기준 |
| --- | --- |
| 시작됨 | 확인창에서 시작하기 |
| 완료 | 조회 성공·고객센터 전화 버튼·송금 확정 |
| 취소 | 사용자 취소 |
| 실패 | 복구 불가능한 업무 실패 |
| 진행 유지 | 잘못된 비밀번호·수정 가능한 금액·음성 실패·경고 결정 대기 |
| 종료 불명 | 새로고침·갑작스러운 종료: 시각 생성 없이 분석 제외 |

## 지표

| 항목 | 정의 |
| --- | --- |
| 재시도 | 같은 단계의 행동을 다시 시도한 횟수 |
| 뒤로 | 이전 단계로 이동한 횟수 |
| 잘못 누름 | 안내 대상 외 유효한 조작을 잘못 선택한 횟수 |
| 경로 이탈 | 해당 방문에서 정해진 업무 흐름 밖 이동 여부: 0/1 |
| 머문 시간 | 종료 − 시작 시각 |
| 제외 지표 | 도움 요청 횟수·독립 수행 여부: 측정 기준 미정 |

## 집계·선정

| 항목 | 규칙 |
| --- | --- |
| 기본 기간 | 한국 날짜 기준 오늘 포함 최근 7일, 시작·종료일 표시 |
| 포함 기준 | 실행 종료 시각: 시작일 00:00 이상, 종료일 다음 날 00:00 미만 |
| 횟수 | 패턴별 완료 건수. 아들·딸 송금은 별도 행 |
| 합계 | 화면 행의 완료 횟수 합 |
| 정렬 | 완료 횟수 내림차순 → 활성 번호 오름차순 → 번호 없음 뒤 → 패턴 식별 순서 |
| 비활성 패턴 | 기간 내 과거 기록 유지 |
| 행 없음 | 기간 내 종료 실행 없음 |
| 완료 0회 | 취소·실패만 있어도 기록 행 가능 |
| 단계 후보 | 기간 내 완료·취소·실패 실행의 방문 |
| 점수 | 재시도 + 뒤로 + 잘못 누름 + 경로 이탈, 같은 단계끼리 합산 |
| 동률 | 평균 시간 긴 순 → 단계 순서 → 단계 식별 순서 |
| 시간 없음 | 실측 시간 뒤로 정렬 |
| 점수 0 | 실제 방문이 있으면 후보 가능, 어려움으로 단정 금지 |
| 방문 없음 | 후보 생성 없음 |

## 화면·안내 개선

| 영역 | 표시·행동 |
| --- | --- |
| 기간·전체 이용 | ‘최근 7일 동안 6번 이용했어요.’ |
| 패턴별 이용 | 많이 쓴 업무부터 이름·번호·횟수 |
| 안내 검토 단계 | 업무·단계·관측 사실 → 안내 비교 |
| 문구 비교 | 현재 / 제안 → 이 안내로 바꾸기·직접 수정 |
| 가족 음성 | 문구 불일치 안내 → 같은 단계 다시 녹음 |
| 비활성 대상 | 기록 유지, ‘지금은 이 업무의 안내를 수정할 수 없어요.’ |
| 자료·실패 | 동의 미완료·거절·자료 없음·로딩·조회 실패 구분 |

- 안내 예: ‘금액을 입력할 때 다시 시도한 기록이 3번 있어요.’
- 점수 0 예: ‘안내 문구를 확인해 보세요.’
- 서버 코드·진단 로그 문구, 건강·인지·사용 의도 평가 제외.

## Agent Notes: Execution

- Create records only with usage consent. On withdrawal, stop new visits/actions; allow terminal cleanup of an existing execution without adding behavior.
- Close the previous visit on a known transition; re-entry gets a new visit number.
- Send cumulative counts, not increments. Repeated totals must not double-count; ignore lower totals and keep deviation true once observed.
- Normal back navigation, anomaly review, and cancellation branches are not deviations. Amount/PIN steps have no highlight target, so do not invent wrong-touch counts.
- Exclude raw clicks, coordinates, input values, full account numbers, PINs, speech, and recordings.
- Transfer finalization owns the financial outcome, last visit, and execution end. Deliver applicable final action totals before closure.
- Retryable PIN errors update totals without closing the visit. Do not submit another finish/update after server finalization.
- Keep selected source consistent with transaction/execution. Query/logging failures after financial success must not resubmit a transfer.
- Inquiry and support calls follow their own completion events; a call click does not prove connection.
- Never invent an end time for abrupt departure.

## Agent Notes: Analysis

- Use the same timezone across UI/server/DB. Accept explicit inclusive date ranges; custom date-picker UI is optional.
- Exclude unended executions entirely. Include all visits of eligible ended executions, even if a visit started before the period.
- Include unended visits in action scores but exclude them from duration averages.
- Aggregate at read time; no extra report table or fixed preview analytics.
- Generate suggestions deterministically by step type and task type for the same period. No valid target means no fabricated suggestion.
- Apply only on explicit choice and only if persisted text still equals the compared text; otherwise refetch/recompare. Disable identical-text application.
- Persist the same script used by manual editing and next execution; retain saved text on failure.
- Preserve historical inactive targets; do not reactivate them for editing.
- Follow [voice mismatch/save rules](guidance-voice.md). Acceptance: [SC-012, 015](validation-scenarios.md).
