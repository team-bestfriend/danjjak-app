# 사람·계좌

[목차](../requirements.md)

## 요구사항

| ID | 기능 | 완료 조건 |
| --- | --- | --- |
| FR-005 | 사람 조회 | 이름·관계·등록 계좌 수·계좌 목록 표시 |
| FR-006 | 사람·계좌 관리 | 사람 추가·수정, 기존 사람의 계좌 추가·수정 |
| FR-007 | 계좌 구분 | 내 계좌와 받는 계좌를 별도 조회·선택 |
| FR-008 | 기본 내 계좌 | 기본 계좌 우선 선택, 이번 송금에서 변경 가능 |
| FR-009 | 보호자 연락처 | 사용자별 전화번호 한 개 조회·수정 |

## 관계·입력

| 대상 | 정보 | 규칙 |
| --- | --- | --- |
| 사용자 | 여러 내 계좌·등록 사람 | 내 계좌와 받는 계좌 구분 |
| 등록 사람 | 이름·관계 | 최초 등록 시 계좌 한 개 포함 |
| 받는 계좌 | 은행·계좌번호·선택적 별칭 | 사람별 여러 개, 계좌별 식별자 |
| 송금 패턴 | 특정 받는 계좌 | 계좌 추가만으로 기존 연결 변경 없음 |
| 내 계좌 | 잔액·모의 비밀번호 비교 자료·기본 여부 | 받는 계좌에 잔액·출금 비밀번호 부여 금지 |

| 입력 | 검증 |
| --- | --- |
| 이름·관계 | 필수, 각각 최대 50자·30자 |
| 은행 | 목록에서 선택, 은행 코드 자동 결정 |
| 계좌번호 | 하이픈 제외 숫자 8–20자리 |
| 하이픈 | 숫자 묶음 사이 단일 하이픈만 허용 |
| 별칭 | 선택, 최대 50자 |
| 중복 | 같은 사람·은행·정규화 계좌번호는 중복 등록 불가 |
| 보호자 번호 | 숫자·선택적 하이픈, 전화 전 표시·확인 |

형식 검증만 수행. 실존 계좌·예금주 확인, 사람·계좌 삭제는 범위 제외.

## 관리·선택 화면

| 화면 | 표시·동작 |
| --- | --- |
| 사람 목록 | 이름·관계·‘등록 계좌 N개’ |
| 사람 상세 | 해당 사람의 계좌 목록·추가·수정 |
| 추가 버튼 | ‘사람 추가’, ‘계좌 추가’ 일반 버튼. 점선 장식 제거 |
| 내 계좌 | 은행 로고/이름·별칭·가린 번호·잔액·기본 표시 |
| 받는 계좌 | ‘민수님의 어느 계좌로 보낼까요?’ |
| 선택 상태 | 체크 + ‘선택됨’ |
| 계좌 한 개 | 정보 확인 후 ‘다음’. 자동 생략 없음 |
| 계좌 여러 개 | 선택한 계좌로 최종 확인·송금 |
| 보호자 미등록 | 연락처 등록 안내 |

## Agent Notes

- Use separate person/account identities; a transfer pattern links to one recipient account.
- An account edit affects only that account. Preserve other accounts and pattern references.
- Reuse the same number validation for registration and direct transfer.
- Mask display numbers; expose input values only where editing requires them.
- Persist mutations before reporting success. Prevent duplicate saves; retain safe input on failure.
- A runtime source/recipient change must not silently update saved defaults or pattern links.
- Without a default, select and explain the first available owned account. Without accounts, route to mock import.
- A missing linked account requires pattern correction; never substitute another recipient.
- A guardian phone number is neither a Kakao recipient ID nor transfer approval.
- Acceptance: [SC-005, 010, 015, 017](validation-scenarios.md).
