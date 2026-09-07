# 공통 UX

[목차](../requirements.md)

## 요구사항

| ID | 항목 | 완료 조건 |
| --- | --- | --- |
| UX-001 | 화면 이동 | 화면·주소를 하나의 라우팅 체계로 관리 |
| UX-002 | 직접 진입·새로고침 | 저장 자료 재조회, 송금 정보 소실 시 설명 후 재시작 |
| UX-003 | 뒤로 가기 | ‘‹ 뒤로’, 실제 이전 단계 이동, 안전한 입력 유지 |
| UX-004 | 처리 중 | 해당 영역 진행 표시, 같은 요청 중복 실행 방지 |
| UX-005 | 오류 복구 | 쉬운 원인·다음 행동, 수정 가능한 입력 유지 |
| UX-006 | 빈 상태 | 자료 없는 이유와 등록·재시도·이동 제공 |
| UX-007 | 입력 검증 | 필수·형식·범위 확인, 입력 옆 수정 안내 |
| UX-008 | 중복 제출 | 로그인·저장·송금·결정·카톡 처리 중 버튼 잠금 |
| UX-009 | 상태 초기화 | 완료·취소·새 송금 시 이전 임시 정보 제거 |
| UX-010 | 팝업 | 닫기·취소·뒤로 통일, 배경 조작 차단, 초점 복귀 |
| UX-011 | 조작·가독성 | 주요 영역 최소 48×48 CSS px, 색상 외 상태 표현 |
| UX-012 | 음성 없는 이용 | 질문·선택·다음 행동 유지, 전체 안내 펼치기 |
| UX-013 | 정확한 동작 표현 | 사용 불가 표시, 확인되지 않은 저장·통화·전송 성공 금지 |
| UX-014 | 결과 표시 | 서버가 확인한 저장값·잔액·결과 사용 |

## 화면 기준

| 항목 | 기준 |
| --- | --- |
| 디자인 프레임 | iPhone 12 Pro Max 세로, 428×926 CSS px |
| 축소 검증 | 320×568, 큰 글씨 |
| 글자 초안 | 본문 20 px, 업무 이름 24 px 이상, 보조 16 px 이상 |
| 정보 우선순위 | 홈·목록·확인창에서 업무 이름 > 단축번호 숫자 |
| 상단·하단 | 뒤로·탭에도 최소 조작 영역 적용 |
| 아이콘·글꼴 | 일관된 체계, 장식 이모티콘 혼용 제거: D-04 |
| 은행 | 로고 + 은행명, 로고 실패 시 은행명 유지 |
| 계좌 선택 | 체크 + ‘선택됨’ |
| 입력·버튼 | 안전 영역·주소창·키보드·큰 글씨에도 겹침·잘림 없음 |

## 하단 탭

| 탭 | 내용 |
| --- | --- |
| 홈 | 음성으로 말하기·단축번호·직접 송금 |
| 단축번호 | 등록·상세·수정·번호 이동·비활성화·음성 편집 |
| 이용 분석 | 횟수·안내 검토 단계·문구 비교·재녹음 |
| 설정 | 사용자·접근성·사람/계좌·보호자·동의·이용방법·로그아웃 |

- 네 탭 유지.
- 송금·녹음 중 탭 노출은 화면 설계에서 조정 가능.
- 진행 중 이탈 시 입력 변경·취소 규칙 적용.

## 화면 문구

| 상황 | 문구 | 행동 |
| --- | --- | --- |
| 음성 입력 | ‘1번이라고 말해 보세요.’ | 말하기 종료·취소 |
| 실행 전 확인 | 업무 이름·번호·설명·송금 대상 | 시작하기·취소 |
| 단계 표시 | ‘송금 1단계 · 보낼 계좌’ | 다음·뒤로 |
| 내 계좌 | ‘어느 내 계좌에서 보낼까요?’ | 선택 후 다음 |
| 내 계좌 설명 | ‘돈이 빠져나갈 내 계좌를 선택해 주세요.’ | — |
| 받는 사람 | ‘누구에게 보낼까요?’ / ‘등록 계좌 2개’ | 선택 후 다음 |
| 받는 계좌 | ‘민수님의 어느 계좌로 보낼까요?’ | 선택 후 다음 |
| 금액 | ‘얼마를 보낼까요?’ / 숫자·한글 | 다음 |
| 최종 확인 | ‘이대로 보낼까요?’ / 내 계좌·받는 사람·받는 계좌·보낼 금액 | ‘50,000원 보내기’ → 본인 확인 |
| 본인 확인 | ‘계좌 비밀번호 4자리를 입력해 주세요.’ | 본인 확인 후 보내기 |
| 이상거래 | ‘한 번 더 확인해 주세요.’ | 다시 확인·계속 보내기·보내지 않기 |
| 완료 | ‘김민수님에게 50,000원을 보냈어요.’ | 홈으로 가기 |

## 음성 영역: D-01 기본안

| 상태·영역 | 표시 |
| --- | --- |
| 재생 중 | 안내 중·멈춤 |
| 재생 후 | 다시 듣기 |
| 전체 문구 | 안내 보기로 펼치기 |
| 항상 유지 | 핵심 질문·입력 설명·오류·주 행동 |
| 제거 | ‘지금 할 일’, ‘자동 TTS’, 중복 대본·긴 파형 상시 표시 |
| 홈 상세 설명 | 설정의 서비스 이용방법·도움말로 이동 |
| 명칭 | AI 음성 / 가족 음성: D-04 |
| 시간 경과 자동 표시 | 미확정, 사용성 검토 |

## 공통 상태 문구

| 상태 | 문구 예 | 행동 |
| --- | --- | --- |
| 로딩 | ‘계좌를 불러오고 있어요.’ | 대기 |
| 등록 없음 | ‘등록된 사람이 없어요.’ | 사람 추가 |
| 기록 없음 | ‘아직 이용 기록이 없어요.’ | 홈으로 가기 |
| 입력 오류 | ‘계좌번호를 다시 확인해 주세요.’ | 수정 |
| 조회·저장 실패 | ‘내용을 불러오지 못했어요.’ / ‘저장하지 못했어요.’ | 다시 시도 |
| 세션 만료 | ‘다시 로그인해 주세요.’ | 로그인 |
| 진행 소실 | ‘송금 정보가 초기화됐어요. 처음부터 다시 진행해 주세요.’ | 송금 시작 |
| 대상 없음 | ‘지금은 이 항목을 사용할 수 없어요.’ | 목록 갱신·돌아가기 |

## Agent Notes

- Treat 428×926 as a design frame, not guaranteed browser height. Keep long text scrollable and primary actions reachable.
- Apply text-size preferences without shrinking touch targets or clipping labels.
- Use actual contiguous step order. Confirmation, anomaly, and result screens are not extra input steps; see [transfer](mock-transfer.md).
- Keep the main question visible when guidance is collapsed. Expanding must not hide the task action; collapsing is independent of playback.
- Never restore a PIN or infer transfer completion from a URL. Unknown routes need a recoverable not-found state.
- Display user-facing errors, not server codes or field names. PIN errors require visual and audio feedback.
- Use [auth gates](auth-settings.md); analysis may show a consent-required state before choices are complete.
- Placeholder image names do not establish approved layouts or assets.
- Acceptance: [SC-014, 018](validation-scenarios.md).
