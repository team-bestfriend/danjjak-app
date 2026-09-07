# 금융 조회

[목차](../requirements.md)

## 요구사항

| ID | 기능 | 완료 조건 |
| --- | --- | --- |
| FR-032 | 모의 금융 조회 | 내 계좌의 잔액·거래·연금·관리비·공과금 조회 |
| FR-033 | 거래 정보 | 입출금·금액·상대방·시각·거래 후 잔액 표시 |
| FR-034 | 고객센터 | 제공된 번호 표시, 사용자 선택 시 전화 앱 연결 |

**단축번호 → 실행 전 확인 → 시작 → 결과 확인 1단계**

| 업무 | 표시·조작 |
| --- | --- |
| 잔액 | 최초 숨김, ‘잔액 보기/숨기기’ |
| 거래내역 | 전체/입금/출금 필터, 최신순 |
| 연금 | 연금 분류 입금 |
| 관리비·공과금 | 해당 분류 거래 |
| 내 계좌 변경 | 같은 결과 화면에서 재조회 |
| 조회 기간 | 선택 계좌의 전체 저장 기간. ‘이번 달’ 고정 문구 금지 |
| 금액 | 쉼표·원 단위, 상세 핵심 금액은 한글 병기 |
| 고객센터 | 번호·‘전화 연결하기’, 컴퓨터에서도 번호 표시 |

## 결과 상태

| 상태 | 안내·완료 |
| --- | --- |
| 정상 자료 | 결과 표시 후 조회 완료 |
| 정상 빈 결과 | 계좌·분류를 설명하고 조회 완료 가능 |
| 계좌 없음 | 불러오기 안내, 완료 아님 |
| 조회 실패 | 재시도, 완료 아님 |
| 고객센터 | 전화 버튼 선택 시 패턴 완료. 실제 연결·상담 성공 의미 아님 |

## Agent Notes

- Query only owned accounts; switching accounts refetches that account's data without adding a template step.
- Derive results from stored mock data, never fixed preview values. Refetch after transfer to show the new debit and balance.
- Do not infer assistance or ability from balance visibility.
- Use the returned support number; no invented fallback.
- Amount formatting: [FR-059](mock-transfer.md). Acceptance: [SC-004, 010](validation-scenarios.md).
