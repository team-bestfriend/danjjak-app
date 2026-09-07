# 단축번호·패턴

[목차](../requirements.md)

## 요구사항

| ID | 기능 | 완료 조건 |
| --- | --- | --- |
| FR-010 | 기본 패턴 | 모의 사용자에게 활성 8개 |
| FR-011 | 번호 제한 | 사용자별 1–12, 중복 없음, 활성 최대 12개 |
| FR-012 | 관리 | 등록·수정·번호 이동/교환·비활성화 |
| FR-013 | 템플릿 | 정해진 업무 템플릿으로 등록 |
| FR-014 | 송금 대상 | 등록 사람의 특정 받는 계좌 연결 |
| FR-015 | 패턴 수정 | 이름·설명·계좌·설명 음성·단계 문구/음성 |
| FR-016 | 시작 확인 | 카드·음성 선택 → 확인창 → 시작하기 |
| FR-017 | 순차 실행 | 저장된 단계 순서·안내 적용 |

## 기본 업무

| 번호 | 업무 | 자료 |
| --- | --- | --- |
| 1 | 아들에게 송금하기 | 아들의 특정 등록 계좌 |
| 2 | 연금 입금 확인 | 기본 내 계좌의 연금 거래 |
| 3 | 관리비 납부 확인 | 기본 내 계좌의 관리비 거래 |
| 4 | 잔액 확인 | 기본 내 계좌 |
| 5 | 거래내역 보기 | 기본 내 계좌 |
| 6 | 고객센터 전화하기 | 제공된 연락처 |
| 7 | 딸에게 송금하기 | 딸의 특정 등록 계좌 |
| 8 | 공과금 납부 확인 | 기본 내 계좌의 공과금 거래 |

- 템플릿 7종: 송금·연금·관리비·잔액·거래내역·고객센터·공과금.
- 아들·딸 송금은 같은 템플릿의 별도 패턴.
- 제외: 자유 업무/단계 설계, 자동이체·카드·예금만기·환율.

## 등록·수정

**템플릿 → 빈 번호 → 송금 대상 계좌 → 이름·설명 → 음성 → 최종 확인·저장**

| 패턴 수정 항목 | 내용 |
| --- | --- |
| 기본 정보 | 번호·이름·연결 계좌 |
| 패턴 설명과 음성 | 설명 문구·AI/가족 선택·미리 듣기·녹음 |
| 단계별 안내와 음성 | 실제 단계 목록·개별 문구·음성·녹음 |
| 최종 확인 | 변경 내용·저장 상태 |

| 입력·상태 | 규칙 |
| --- | --- |
| 이름 | 공백만 불가, 최대 50자 |
| 설명·단계 문구 | 최대 500자, 상세는 [음성 편집](guidance-voice.md) |
| 송금 대상 | 사람 선택 후 특정 계좌 확인. 한 개도 확인 |
| 등록 실패 | 번호 중복·범위 초과·13번째 활성·사용 불가 템플릿·계좌 누락 안내 |
| 분석에서 수정 | 패턴 수정의 해당 단계 편집기로 연결 |

## 홈·번호 관리

| 동작 | 결과 |
| --- | --- |
| 홈 페이지 | 1–4 / 5–8 / 9–12, 버튼·가로 넘기기 |
| 카드·목록·확인창 | 업무 이름을 번호보다 크게 |
| 빈 번호 | 등록 진입 |
| 빈 번호로 이동 | 해당 패턴 번호 변경 |
| 사용 중 번호로 이동 | 두 업무·번호 표시 → 교환 확인 |
| 교환 취소·실패 | 이전 순서 유지·복구 |
| 성공 후 되돌리기 | 가능. 교환 전 확인은 별도 유지 |
| 비활성화 | 번호 해제·재사용, 과거 실행/단계 기록 보존 |
| 비활성 과거 항목 | 분석 가능, 실행·수정 불가 |
| 최근 이용 | 실제 시각 또는 기록 없음. 고정 ‘오늘’ 금지 |

## 실행 단계

| 업무 | 단계 |
| --- | --- |
| 등록 송금 | 내 계좌 → 받는 사람 → 받는 계좌 → 금액 → 최종 확인 → 본인 확인 |
| 금융 조회 | 결과 확인 1단계 |
| 고객센터 | 연락처 확인·전화 걸기 1단계 |

- 연결 사람·계좌는 미리 선택 가능, 확인 화면 생략 불가.
- ‘아들에게 송금하기’도 1→2→3의 연속 순서.
- 시작 확인창·경고·완료는 템플릿 단계에 포함하지 않음.

## Agent Notes

- Persist the complete active order consistently; require swap confirmation for both number picking and drag.
- Distinguish tap, scroll, swipe, and long press. Ending a gesture must not start a task.
- Refetch persisted detail for editing and validate availability before start. Never execute stale inactive targets.
- Confirmation shows title, number, description, and transfer recipient/account. Use saved pre-start voice; playback completion is not required.
- Create an execution only after Start and only with recording consent. Closing confirmation creates no execution or financial request.
- Follow returned step identities/order, not hardcoded screen order. Every default/new target needs a valid template default.
- Preserve parent drafts and target-specific edits; use [voice save rules](guidance-voice.md).
- Complete only after the actual task result; follow [execution boundaries](usage-analysis.md).
- Acceptance: [SC-003, 004, 013, 017](validation-scenarios.md).
