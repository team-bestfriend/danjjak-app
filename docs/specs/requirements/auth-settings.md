# 인증·설정

[목차](../requirements.md)

## 요구사항

| ID | 기능 | 완료 조건 |
| --- | --- | --- |
| FR-001 | 카카오 로그인 | 인증 성공·취소·실패 구분 |
| FR-002 | 모의 사용자 연결 | 같은 카카오 사용자에 같은 모의 사용자 연결 |
| FR-003 | 내 정보 | 이름·동의·접근성·계좌 준비 상태 조회 |
| FR-004 | 접근성 | 글씨 크기·안내 속도·기본 음성 저장·적용 |
| FR-053 | 선택 동의 | 이용 기록·보호자 알림을 독립 선택. 둘 다 거절 가능 |
| FR-057 | 세션·로그아웃 | 새로고침 시 세션 확인, 로그아웃·만료 시 로그인 안내 |
| FR-058 | 모의 계좌 불러오기 | 준비된 모의 본인 계좌 선택·추가 |
| FR-060 | 서비스 이용방법 | 번호·음성·가족 안내·모의 금융·카톡 시연 설명 |

## 첫 이용

**소개 → 카카오 로그인 → 선택 동의 → 내 계좌 불러오기 → 홈**

| 소개 메시지 | 설명 |
| --- | --- |
| 단축번호로 금융 업무 | 자주 하는 일을 번호로 찾아요. |
| 가족이 준비한 안내 | 익숙한 목소리로 안내를 들어요. |
| 이상 거래 확인 | 큰 금액이나 반복 송금은 한 번 더 확인해요. |

| 상황 | 처리 |
| --- | --- |
| 재로그인 | 완료한 단계 생략, 미완료 단계부터 진행 |
| 로그인 취소·실패 | 재시도 제공, 임의 사용자·세션 생성 금지 |
| 연결할 모의 사용자 없음 | 준비 필요 안내 |
| 동의 저장 실패 | 선택값 유지·재시도 |
| 로그아웃 | 세션 종료, 저장된 설정·금융 기록 유지 |

## 모의 계좌 불러오기

1. ‘내 계좌를 불러올까요?’와 ‘실제 은행에 연결하지 않는 연습용 계좌예요.’ 표시.
2. 후보의 은행·별칭·가린 계좌번호·준비 상태 확인 및 선택.
3. ‘계좌 불러오기’ → 저장 성공 → 홈.

| 상태 | 동작 |
| --- | --- |
| 추가 계좌 | 설정 → 내 계좌 관리 → 계좌 불러오기 |
| 재요청·재로그인 | 기존 계좌·잔액·거래·패턴 유지, 중복 추가 없음 |
| 후보 없음 | ‘불러올 계좌가 없어요.’ |
| 실패 | 오류 안내·재시도 |
| 계좌 준비 전 | 금융 실행 제한, 설정·이용방법·로그아웃 허용 |

## 설정

| 항목 | 내용 |
| --- | --- |
| 내 정보 | 이름만 표시. ‘단짝 시연 사용자’ 삭제 |
| 글씨 크기 | 작게 / 보통 / 크게, 미리보기 |
| 안내 속도 | 느리게 / 보통 / 빠르게, 미리듣기 |
| 기본 안내 음성 | AI 음성 / 가족 음성 |
| 사람 및 계좌 관리 | 사람 추가·수정, 기존 사람의 계좌 추가·수정 |
| 내 계좌 관리 | 본인 계좌 확인·모의 계좌 추가 |
| 보호자 연락처 | 전화번호 조회·수정 |
| 선택 동의 | 이용 기록·보호자 알림 재선택 |
| 서비스 이용방법 | 홈에서 줄인 상세 설명 |
| 로그아웃 | 세션 종료 |

## Agent Notes

- Persist consent completion separately from both optional values; false/false is valid.
- Without usage consent, collect no new pattern/step behavior. Financial transactions remain separate.
- Explain self-delivery in guardian consent and help; follow [notification rules](fds-guardian.md).
- Keep tokens server-side; exclude them from UI, URLs, and logs. Prevent duplicate auth/settings submissions.
- Global voice settings apply only to targets without an explicit override. Preserve recordings and explicit choices.
- Preserve drafts on save failure; never report unsaved values as persisted. Do not request real banking credentials.
- Acceptance: [SC-001, 002, 014, 016, 018](validation-scenarios.md).
