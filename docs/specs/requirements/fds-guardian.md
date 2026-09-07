# 이상거래·보호자 대응

[목차](../requirements.md)

## 요구사항

| ID | 기능 | 완료 조건 |
| --- | --- | --- |
| FR-035 | 공통 FDS | 패턴·직접 송금에 같은 서버 판정 |
| FR-036 | 고액 | 10,000,000원 이상 |
| FR-037 | 반복 | 이전 10분 내 완료 출금 송금 2건 이상 |
| FR-038 | 위험 단계 | 사유 0개 정상 / 1개 주의 / 2개 높은 주의 |
| FR-039 | 경고 | 위험 정도·전체 사유·받는 사람·계좌·금액 표시 |
| FR-040 | 사용자 결정 | 다시 확인·계속 보내기·보내지 않기 |
| FR-041 | 판정 기록 | 이상 시도당 하나의 기록 유지 |
| FR-042 | 카톡 시연 | 높은 주의 + 동의 + 명시적 선택, 실제/모의 결과 구별 |
| FR-043 | 보호자 전화 | 저장 번호 확인 후 전화 앱 연결 |

## 판정·화면

| 항목 | 기준 |
| --- | --- |
| 판정 주체 | 서버 |
| 시간 범위 | 판정 시각의 10분 전부터 현재까지 양 끝 포함, 현재 시도 제외 |
| 합산 | 같은 사용자의 모든 내 계좌, 직접·패턴 완료 송금 |
| 제외 | 실패·취소·결정 대기, 이용 기록 동의 여부는 무관 |
| FDS 아님 | 새 계좌·경로 이탈·잘못 누름·소요 시간·분석 점수 |
| 정상 | 경고 기록 없이 송금 처리 |
| 주의·높은 주의 | 결정 전 차감 없음, 사유가 둘이어도 판정 기록 하나 |

| 화면 항목 | 표시 |
| --- | --- |
| 제목 | ‘한 번 더 확인해 주세요.’ |
| 정도 | ‘주의’ / ‘높은 주의’, 색상 외 글자 구분 |
| 고액 이유 | ‘1,000만원 이상 보내려고 해요.’ |
| 반복 이유 | ‘최근 10분 동안 2번 보냈어요.’ |
| 대상·금액 | 받는 사람·은행·계좌, 숫자·한글 금액 |
| 행동 | 다시 확인 / 계속 보내기 / 보내지 않기 |
| 연락 | 보호자 전화, 높은 주의일 때 카톡 보내기 |

‘처음 보내는 계좌예요.’는 팀 문구 예시이며 이번 FDS 조건에 추가하지 않음.

## 결정

| 행동 | 결과 |
| --- | --- |
| 다시 확인 | 같은 시도의 정보 읽기 전용 확인, 재확인 여부 기록 |
| 계좌·금액 변경 | 현재 시도 취소 후 새 송금 |
| 계속 보내기 | 잔액 재확인 → 차감·거래 확정 → 완료 |
| 보내지 않기 | 취소 확정, 차감·거래 없음 |
| 중복 결정 | 저장된 결과 표시, 금융 처리 반복 없음 |
| 요청 실패 | 판정 식별 정보·안전한 입력 유지, 재시도 |

## 카톡 시연

| 항목 | 확정 내용 |
| --- | --- |
| 버튼 | **보호자에게 카톡 보내기** |
| 실제 수신자 | **로그인한 본인 계정: 나에게 보내기** |
| 사전 설명 | 동의·이용방법·전송 확인에 본인 전송 시연임을 안내 |
| 조건 | 본인 소유·미결정·높은 주의 + 보호자 공유 동의 + 버튼 선택 |
| 동의 없음 | 설정 안내, 실제·모의 전송 없음 |
| 전화번호 | 카카오 수신자 식별자로 사용하지 않음 |

| 결과 | 사용자 안내 | 실제 전송 시각 |
| --- | --- | --- |
| 전송 성공 | ‘시연 알림을 내 카카오톡으로 보냈어요.’ | 저장 |
| 자격 정보 없음 | ‘실제 전송 없이 알림 보내기를 시연했어요.’ | 없음 |
| 실패 후 모의 전환 | ‘카카오톡 전송에 실패해 모의 알림으로 시연했어요.’ | 없음 |

## 전화

- 저장된 보호자 번호 표시 → 사용자 확인·선택 → 전화 앱.
- 미등록: 연락처 등록 안내. 통화 미지원: 번호 표시.
- 전화·카톡 결과는 보호자 승인이나 실제 확인의 증거가 아님.

## Agent Notes

- Render returned risk and reasons; never infer extra rules client-side. Family guidance must not hide system reasons.
- Reuse the same anomaly identity across recheck, notification, and decision. Do not create another record on resubmission/re-entry for the same active attempt.
- Notification requires server-side ownership, unresolved HIGH risk, consent, and explicit choice. A phone contact is insufficient.
- Include demo context, reasons, recipient, amount, and masked account in the message; exclude PIN/full account numbers.
- Reuse an existing actual-send result without resending while unresolved. Reject sends after resolution or below HIGH. Mock outcomes have no actual-send timestamp or durable delivery history.
- Audio/notification failure must not block continue, cancel, or calling. Lock mutation controls while pending.
- Calls do not confirm connection or change the transfer decision automatically.
- References: [UX](shared-ux.md), [provider review](design-decisions.md), [SC-008–010, 015](validation-scenarios.md).
