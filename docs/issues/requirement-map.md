# 요구사항 → 실제 이슈 추적

- 요구사항 정의 **144개**: FR 61 · UX 14 · NFR 12 · SC 18 · CH 34 · D 5. 모든 ID를 보존했습니다.
- 첫 이슈가 주 담당이며 뒤 이슈는 같은 요구사항의 연계 구현·검증입니다. 한 이슈가 여러 요구사항을 완결된 결과로 묶습니다.
- SC는 실행 후 확인할 계획이고 CH/D는 설계 변경 상태입니다. 설계 검증과 본선 기능 검증을 구분합니다.
- FR-061·CH-13·D-03은 선택 검토로, 필수 기능 완료에 포함하지 않습니다.
- [기능별 Epic·선행 관계](issue-map.md) · [명세 목차](../specs/requirements.md)

## FR

| ID | 기준 명세·항목 | 상태 | 실제 이슈: 주 담당 → 연계 |
| --- | --- | --- | --- |
| FR-001 | [카카오 로그인](../specs/requirements/auth-settings.md) | 목표 명세 · 구현 대기 | [#17](https://github.com/team-bestfriend/danjjak-app/issues/17) |
| FR-002 | [모의 사용자 연결](../specs/requirements/auth-settings.md) | 목표 명세 · 구현 대기 | [#17](https://github.com/team-bestfriend/danjjak-app/issues/17) |
| FR-003 | [내 정보](../specs/requirements/auth-settings.md) | 목표 명세 · 구현 대기 | [#18](https://github.com/team-bestfriend/danjjak-app/issues/18), [#17](https://github.com/team-bestfriend/danjjak-app/issues/17) |
| FR-004 | [접근성](../specs/requirements/auth-settings.md) | 목표 명세 · 구현 대기 | [#18](https://github.com/team-bestfriend/danjjak-app/issues/18) |
| FR-005 | [사람 조회](../specs/requirements/people-accounts.md) | 목표 명세 · 구현 대기 | [#21](https://github.com/team-bestfriend/danjjak-app/issues/21) |
| FR-006 | [사람·계좌 관리](../specs/requirements/people-accounts.md) | 목표 명세 · 구현 대기 | [#21](https://github.com/team-bestfriend/danjjak-app/issues/21) |
| FR-007 | [계좌 구분](../specs/requirements/people-accounts.md) | 목표 명세 · 구현 대기 | [#19](https://github.com/team-bestfriend/danjjak-app/issues/19), [#21](https://github.com/team-bestfriend/danjjak-app/issues/21) |
| FR-008 | [기본 내 계좌](../specs/requirements/people-accounts.md) | 목표 명세 · 구현 대기 | [#19](https://github.com/team-bestfriend/danjjak-app/issues/19), [#32](https://github.com/team-bestfriend/danjjak-app/issues/32) |
| FR-009 | [보호자 연락처](../specs/requirements/people-accounts.md) | [저장·조회 구현 및 검증](../verification/issue-20-guardian-contact.md) · 실제 인증 연결 대기 | [#20](https://github.com/team-bestfriend/danjjak-app/issues/20) |
| FR-010 | [기본 패턴](../specs/requirements/shortcuts-patterns.md) | 목표 명세 · 구현 대기 | [#22](https://github.com/team-bestfriend/danjjak-app/issues/22), [#13](https://github.com/team-bestfriend/danjjak-app/issues/13) |
| FR-011 | [번호 제한](../specs/requirements/shortcuts-patterns.md) | 목표 명세 · 구현 대기 | [#22](https://github.com/team-bestfriend/danjjak-app/issues/22), [#23](https://github.com/team-bestfriend/danjjak-app/issues/23) |
| FR-012 | [관리](../specs/requirements/shortcuts-patterns.md) | 목표 명세 · 구현 대기 | [#23](https://github.com/team-bestfriend/danjjak-app/issues/23), [#22](https://github.com/team-bestfriend/danjjak-app/issues/22) |
| FR-013 | [템플릿](../specs/requirements/shortcuts-patterns.md) | 목표 명세 · 구현 대기 | [#22](https://github.com/team-bestfriend/danjjak-app/issues/22) |
| FR-014 | [송금 대상](../specs/requirements/shortcuts-patterns.md) | 목표 명세 · 구현 대기 | [#22](https://github.com/team-bestfriend/danjjak-app/issues/22), [#21](https://github.com/team-bestfriend/danjjak-app/issues/21) |
| FR-015 | [패턴 수정](../specs/requirements/shortcuts-patterns.md) | 목표 명세 · 구현 대기 | [#22](https://github.com/team-bestfriend/danjjak-app/issues/22) |
| FR-016 | [시작 확인](../specs/requirements/shortcuts-patterns.md) | 목표 명세 · 구현 대기 | [#26](https://github.com/team-bestfriend/danjjak-app/issues/26), [#23](https://github.com/team-bestfriend/danjjak-app/issues/23), [#31](https://github.com/team-bestfriend/danjjak-app/issues/31) |
| FR-017 | [순차 실행](../specs/requirements/shortcuts-patterns.md) | 목표 명세 · 구현 대기 | [#26](https://github.com/team-bestfriend/danjjak-app/issues/26), [#22](https://github.com/team-bestfriend/danjjak-app/issues/22) |
| FR-018 | [조작 강조](../specs/requirements/guidance-voice.md) | 목표 명세 · 구현 대기 | [#26](https://github.com/team-bestfriend/danjjak-app/issues/26), [#27](https://github.com/team-bestfriend/danjjak-app/issues/27) |
| FR-019 | [문구 편집](../specs/requirements/guidance-voice.md) | 목표 명세 · 구현 대기 | [#25](https://github.com/team-bestfriend/danjjak-app/issues/25) |
| FR-020 | [기본 문구](../specs/requirements/guidance-voice.md) | 목표 명세 · 구현 대기 | [#25](https://github.com/team-bestfriend/danjjak-app/issues/25), [#13](https://github.com/team-bestfriend/danjjak-app/issues/13), [#22](https://github.com/team-bestfriend/danjjak-app/issues/22), [#26](https://github.com/team-bestfriend/danjjak-app/issues/26) |
| FR-021 | [화면 안내](../specs/requirements/guidance-voice.md) | 목표 명세 · 구현 대기 | [#27](https://github.com/team-bestfriend/danjjak-app/issues/27) |
| FR-022 | [AI 음성](../specs/requirements/guidance-voice.md) | 목표 명세 · 구현 대기 | [#27](https://github.com/team-bestfriend/danjjak-app/issues/27) |
| FR-023 | [가족 음성](../specs/requirements/guidance-voice.md) | 목표 명세 · 구현 대기 | [#30](https://github.com/team-bestfriend/danjjak-app/issues/30) |
| FR-024 | [녹음](../specs/requirements/guidance-voice.md) | 목표 명세 · 구현 대기 | [#30](https://github.com/team-bestfriend/danjjak-app/issues/30) |
| FR-025 | [교체](../specs/requirements/guidance-voice.md) | 목표 명세 · 구현 대기 | [#30](https://github.com/team-bestfriend/danjjak-app/issues/30) |
| FR-026 | [대체 안내](../specs/requirements/guidance-voice.md) | 목표 명세 · 구현 대기 | [#27](https://github.com/team-bestfriend/danjjak-app/issues/27), [#30](https://github.com/team-bestfriend/danjjak-app/issues/30) |
| FR-027 | [음성 입력](../specs/requirements/guidance-voice.md) | 목표 명세 · 구현 대기 | [#31](https://github.com/team-bestfriend/danjjak-app/issues/31) |
| FR-028 | [송금 정보](../specs/requirements/mock-transfer.md) | 목표 명세 · 구현 대기 | [#32](https://github.com/team-bestfriend/danjjak-app/issues/32), [#28](https://github.com/team-bestfriend/danjjak-app/issues/28) |
| FR-029 | [직접 송금](../specs/requirements/mock-transfer.md) | 목표 명세 · 구현 대기 | [#36](https://github.com/team-bestfriend/danjjak-app/issues/36), [#28](https://github.com/team-bestfriend/danjjak-app/issues/28) |
| FR-030 | [본인 확인](../specs/requirements/mock-transfer.md) | 목표 명세 · 구현 대기 | [#32](https://github.com/team-bestfriend/danjjak-app/issues/32), [#28](https://github.com/team-bestfriend/danjjak-app/issues/28) |
| FR-031 | [모의 처리](../specs/requirements/mock-transfer.md) | 목표 명세 · 구현 대기 | [#28](https://github.com/team-bestfriend/danjjak-app/issues/28), [#32](https://github.com/team-bestfriend/danjjak-app/issues/32) |
| FR-032 | [모의 금융 조회](../specs/requirements/financial-inquiries.md) | 목표 명세 · 구현 대기 | [#33](https://github.com/team-bestfriend/danjjak-app/issues/33) |
| FR-033 | [거래 정보](../specs/requirements/financial-inquiries.md) | 목표 명세 · 구현 대기 | [#33](https://github.com/team-bestfriend/danjjak-app/issues/33) |
| FR-034 | [고객센터](../specs/requirements/financial-inquiries.md) | 목표 명세 · 구현 대기 | [#34](https://github.com/team-bestfriend/danjjak-app/issues/34) |
| FR-035 | [공통 FDS](../specs/requirements/fds-guardian.md) | 목표 명세 · 구현 대기 | [#16](https://github.com/team-bestfriend/danjjak-app/issues/16), [#28](https://github.com/team-bestfriend/danjjak-app/issues/28) |
| FR-036 | [고액](../specs/requirements/fds-guardian.md) | 목표 명세 · 구현 대기 | [#16](https://github.com/team-bestfriend/danjjak-app/issues/16) |
| FR-037 | [반복](../specs/requirements/fds-guardian.md) | 목표 명세 · 구현 대기 | [#16](https://github.com/team-bestfriend/danjjak-app/issues/16) |
| FR-038 | [위험 단계](../specs/requirements/fds-guardian.md) | 목표 명세 · 구현 대기 | [#16](https://github.com/team-bestfriend/danjjak-app/issues/16) |
| FR-039 | [경고](../specs/requirements/fds-guardian.md) | 목표 명세 · 구현 대기 | [#37](https://github.com/team-bestfriend/danjjak-app/issues/37) |
| FR-040 | [사용자 결정](../specs/requirements/fds-guardian.md) | 목표 명세 · 구현 대기 | [#37](https://github.com/team-bestfriend/danjjak-app/issues/37) |
| FR-041 | [판정 기록](../specs/requirements/fds-guardian.md) | 목표 명세 · 구현 대기 | [#37](https://github.com/team-bestfriend/danjjak-app/issues/37), [#16](https://github.com/team-bestfriend/danjjak-app/issues/16), [#28](https://github.com/team-bestfriend/danjjak-app/issues/28) |
| FR-042 | [카톡 시연](../specs/requirements/fds-guardian.md) | 목표 명세 · 구현 대기 | [#39](https://github.com/team-bestfriend/danjjak-app/issues/39) |
| FR-043 | [보호자 전화](../specs/requirements/fds-guardian.md) | [번호 확인·명시적 전화 동작 구현](../verification/issue-20-guardian-contact.md) · 실기기 연결 대기 | [#20](https://github.com/team-bestfriend/danjjak-app/issues/20) |
| FR-044 | [실행 기록](../specs/requirements/usage-analysis.md) | 목표 명세 · 구현 대기 | [#24](https://github.com/team-bestfriend/danjjak-app/issues/24), [#28](https://github.com/team-bestfriend/danjjak-app/issues/28) |
| FR-045 | [단계 방문](../specs/requirements/usage-analysis.md) | 목표 명세 · 구현 대기 | [#24](https://github.com/team-bestfriend/danjjak-app/issues/24), [#28](https://github.com/team-bestfriend/danjjak-app/issues/28) |
| FR-046 | [행동 합계](../specs/requirements/usage-analysis.md) | 목표 명세 · 구현 대기 | [#24](https://github.com/team-bestfriend/danjjak-app/issues/24), [#28](https://github.com/team-bestfriend/danjjak-app/issues/28) |
| FR-047 | [시간](../specs/requirements/usage-analysis.md) | 목표 명세 · 구현 대기 | [#24](https://github.com/team-bestfriend/danjjak-app/issues/24), [#28](https://github.com/team-bestfriend/danjjak-app/issues/28) |
| FR-048 | [이용 횟수](../specs/requirements/usage-analysis.md) | 목표 명세 · 구현 대기 | [#29](https://github.com/team-bestfriend/danjjak-app/issues/29) |
| FR-049 | [안내 검토 단계](../specs/requirements/usage-analysis.md) | 목표 명세 · 구현 대기 | [#29](https://github.com/team-bestfriend/danjjak-app/issues/29) |
| FR-050 | [문구 제안](../specs/requirements/usage-analysis.md) | 목표 명세 · 구현 대기 | [#38](https://github.com/team-bestfriend/danjjak-app/issues/38) |
| FR-051 | [비교·적용](../specs/requirements/usage-analysis.md) | 목표 명세 · 구현 대기 | [#38](https://github.com/team-bestfriend/danjjak-app/issues/38) |
| FR-052 | [재녹음](../specs/requirements/usage-analysis.md) | 목표 명세 · 구현 대기 | [#38](https://github.com/team-bestfriend/danjjak-app/issues/38) |
| FR-053 | [선택 동의](../specs/requirements/auth-settings.md) | 목표 명세 · 구현 대기 | [#18](https://github.com/team-bestfriend/danjjak-app/issues/18), [#24](https://github.com/team-bestfriend/danjjak-app/issues/24), [#39](https://github.com/team-bestfriend/danjjak-app/issues/39) |
| FR-054 | [설명 음성](../specs/requirements/guidance-voice.md) | 목표 명세 · 구현 대기 | [#25](https://github.com/team-bestfriend/danjjak-app/issues/25), [#30](https://github.com/team-bestfriend/danjjak-app/issues/30) |
| FR-055 | [단계 음성](../specs/requirements/guidance-voice.md) | 목표 명세 · 구현 대기 | [#25](https://github.com/team-bestfriend/danjjak-app/issues/25), [#30](https://github.com/team-bestfriend/danjjak-app/issues/30) |
| FR-056 | [공통 대본](../specs/requirements/guidance-voice.md) | 목표 명세 · 구현 대기 | [#25](https://github.com/team-bestfriend/danjjak-app/issues/25), [#27](https://github.com/team-bestfriend/danjjak-app/issues/27), [#30](https://github.com/team-bestfriend/danjjak-app/issues/30) |
| FR-057 | [세션·로그아웃](../specs/requirements/auth-settings.md) | 목표 명세 · 구현 대기 | [#17](https://github.com/team-bestfriend/danjjak-app/issues/17) |
| FR-058 | [모의 계좌 불러오기](../specs/requirements/auth-settings.md) | 목표 명세 · 구현 대기 | [#19](https://github.com/team-bestfriend/danjjak-app/issues/19) |
| FR-059 | [금액 표시](../specs/requirements/mock-transfer.md) | 목표 명세 · 구현 대기 | [#32](https://github.com/team-bestfriend/danjjak-app/issues/32), [#36](https://github.com/team-bestfriend/danjjak-app/issues/36), [#37](https://github.com/team-bestfriend/danjjak-app/issues/37), [#33](https://github.com/team-bestfriend/danjjak-app/issues/33) |
| FR-060 | [서비스 이용방법](../specs/requirements/auth-settings.md) | 목표 명세 · 구현 대기 | [#18](https://github.com/team-bestfriend/danjjak-app/issues/18) |
| FR-061 | [미리 생성](../specs/requirements/guidance-voice.md) | 선택 검토 · 미채택 | [#35](https://github.com/team-bestfriend/danjjak-app/issues/35) |

## UX

| ID | 기준 명세·항목 | 상태 | 실제 이슈: 주 담당 → 연계 |
| --- | --- | --- | --- |
| UX-001 | [화면 이동](../specs/requirements/shared-ux.md) | 목표 명세 · 구현 대기 | [#14](https://github.com/team-bestfriend/danjjak-app/issues/14) |
| UX-002 | [직접 진입·새로고침](../specs/requirements/shared-ux.md) | 목표 명세 · 구현 대기 | [#15](https://github.com/team-bestfriend/danjjak-app/issues/15) |
| UX-003 | [뒤로 가기](../specs/requirements/shared-ux.md) | 목표 명세 · 구현 대기 | [#14](https://github.com/team-bestfriend/danjjak-app/issues/14) |
| UX-004 | [처리 중](../specs/requirements/shared-ux.md) | 목표 명세 · 구현 대기 | [#15](https://github.com/team-bestfriend/danjjak-app/issues/15) |
| UX-005 | [오류 복구](../specs/requirements/shared-ux.md) | 목표 명세 · 구현 대기 | [#15](https://github.com/team-bestfriend/danjjak-app/issues/15) |
| UX-006 | [빈 상태](../specs/requirements/shared-ux.md) | 목표 명세 · 구현 대기 | [#15](https://github.com/team-bestfriend/danjjak-app/issues/15) |
| UX-007 | [입력 검증](../specs/requirements/shared-ux.md) | 목표 명세 · 구현 대기 | [#15](https://github.com/team-bestfriend/danjjak-app/issues/15) |
| UX-008 | [중복 제출](../specs/requirements/shared-ux.md) | 목표 명세 · 구현 대기 | [#15](https://github.com/team-bestfriend/danjjak-app/issues/15) |
| UX-009 | [상태 초기화](../specs/requirements/shared-ux.md) | 목표 명세 · 구현 대기 | [#15](https://github.com/team-bestfriend/danjjak-app/issues/15) |
| UX-010 | [팝업](../specs/requirements/shared-ux.md) | 목표 명세 · 구현 대기 | [#15](https://github.com/team-bestfriend/danjjak-app/issues/15) |
| UX-011 | [조작·가독성](../specs/requirements/shared-ux.md) | 목표 명세 · 구현 대기 | [#14](https://github.com/team-bestfriend/danjjak-app/issues/14) |
| UX-012 | [음성 없는 이용](../specs/requirements/shared-ux.md) | 목표 명세 · 구현 대기 | [#14](https://github.com/team-bestfriend/danjjak-app/issues/14) |
| UX-013 | [정확한 동작 표현](../specs/requirements/shared-ux.md) | 목표 명세 · 구현 대기 | [#15](https://github.com/team-bestfriend/danjjak-app/issues/15) |
| UX-014 | [결과 표시](../specs/requirements/shared-ux.md) | 목표 명세 · 구현 대기 | [#15](https://github.com/team-bestfriend/danjjak-app/issues/15) |

## NFR

| ID | 기준 명세·항목 | 상태 | 실제 이슈: 주 담당 → 연계 |
| --- | --- | --- | --- |
| NFR-001 | [기술](../specs/requirements/delivery-constraints.md) | 목표 명세 · 구현 대기 | [#12](https://github.com/team-bestfriend/danjjak-app/issues/12) |
| NFR-002 | [저장](../specs/requirements/delivery-constraints.md) | 목표 명세 · 구현 대기 | [#13](https://github.com/team-bestfriend/danjjak-app/issues/13) |
| NFR-003 | [실행 재현](../specs/requirements/delivery-constraints.md) | 목표 명세 · 구현 대기 | [#12](https://github.com/team-bestfriend/danjjak-app/issues/12), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| NFR-004 | [HTTP 계약](../specs/requirements/delivery-constraints.md) | [공동 계약·정적 검증 완료](../../contracts/validation.md) · 실제 FE/BE 연동 대기 | [#11](https://github.com/team-bestfriend/danjjak-app/issues/11) |
| NFR-005 | [접근성](../specs/requirements/delivery-constraints.md) | 목표 명세 · 구현 대기 | [#14](https://github.com/team-bestfriend/danjjak-app/issues/14), [#15](https://github.com/team-bestfriend/danjjak-app/issues/15), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| NFR-006 | [민감 정보](../specs/requirements/delivery-constraints.md) | 목표 명세 · 구현 대기 | [#40](https://github.com/team-bestfriend/danjjak-app/issues/40), [#13](https://github.com/team-bestfriend/danjjak-app/issues/13), [#17](https://github.com/team-bestfriend/danjjak-app/issues/17), [#30](https://github.com/team-bestfriend/danjjak-app/issues/30) |
| NFR-007 | [반복 시연](../specs/requirements/delivery-constraints.md) | 목표 명세 · 구현 대기 | [#40](https://github.com/team-bestfriend/danjjak-app/issues/40), [#39](https://github.com/team-bestfriend/danjjak-app/issues/39), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| NFR-008 | [규모](../specs/requirements/delivery-constraints.md) | 목표 명세 · 구현 대기 | [#11](https://github.com/team-bestfriend/danjjak-app/issues/11), [#12](https://github.com/team-bestfriend/danjjak-app/issues/12) |
| NFR-009 | [검증](../specs/requirements/delivery-constraints.md) | 목표 명세 · 구현 대기 | [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| NFR-010 | [외부 서비스](../specs/requirements/delivery-constraints.md) | 목표 명세 · 구현 대기 | [#40](https://github.com/team-bestfriend/danjjak-app/issues/40), [#27](https://github.com/team-bestfriend/danjjak-app/issues/27), [#39](https://github.com/team-bestfriend/danjjak-app/issues/39) |
| NFR-011 | [로그](../specs/requirements/delivery-constraints.md) | 목표 명세 · 구현 대기 | [#40](https://github.com/team-bestfriend/danjjak-app/issues/40), [#17](https://github.com/team-bestfriend/danjjak-app/issues/17), [#24](https://github.com/team-bestfriend/danjjak-app/issues/24) |
| NFR-012 | [화면](../specs/requirements/delivery-constraints.md) | 목표 명세 · 구현 대기 | [#41](https://github.com/team-bestfriend/danjjak-app/issues/41), [#14](https://github.com/team-bestfriend/danjjak-app/issues/14) |

## SC

| ID | 기준 명세·항목 | 상태 | 실제 이슈: 주 담당 → 연계 |
| --- | --- | --- | --- |
| SC-001 | [처음 로그인·동의](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#18](https://github.com/team-bestfriend/danjjak-app/issues/18), [#17](https://github.com/team-bestfriend/danjjak-app/issues/17), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-002 | [재로그인·로그아웃](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#17](https://github.com/team-bestfriend/danjjak-app/issues/17), [#18](https://github.com/team-bestfriend/danjjak-app/issues/18), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-003 | [패턴 관리](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#23](https://github.com/team-bestfriend/danjjak-app/issues/23), [#22](https://github.com/team-bestfriend/danjjak-app/issues/22), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-004 | [실행·단계 안내·조회](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#26](https://github.com/team-bestfriend/danjjak-app/issues/26), [#24](https://github.com/team-bestfriend/danjjak-app/issues/24), [#33](https://github.com/team-bestfriend/danjjak-app/issues/33), [#34](https://github.com/team-bestfriend/danjjak-app/issues/34), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-005 | [정상 등록 송금](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#32](https://github.com/team-bestfriend/danjjak-app/issues/32), [#19](https://github.com/team-bestfriend/danjjak-app/issues/19), [#28](https://github.com/team-bestfriend/danjjak-app/issues/28), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-006 | [직접 입력 송금](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#36](https://github.com/team-bestfriend/danjjak-app/issues/36), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-007 | [송금 오류·음성](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#28](https://github.com/team-bestfriend/danjjak-app/issues/28), [#27](https://github.com/team-bestfriend/danjjak-app/issues/27), [#32](https://github.com/team-bestfriend/danjjak-app/issues/32), [#40](https://github.com/team-bestfriend/danjjak-app/issues/40), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-008 | [주의·경계값](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#16](https://github.com/team-bestfriend/danjjak-app/issues/16), [#37](https://github.com/team-bestfriend/danjjak-app/issues/37), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-009 | [높은 주의·카톡](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#39](https://github.com/team-bestfriend/danjjak-app/issues/39), [#16](https://github.com/team-bestfriend/danjjak-app/issues/16), [#37](https://github.com/team-bestfriend/danjjak-app/issues/37), [#40](https://github.com/team-bestfriend/danjjak-app/issues/40), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-010 | [보호자·고객센터 전화](../specs/requirements/validation-scenarios.md) | [HTTP·DB·화면 일부 검증](../verification/issue-20-guardian-contact.md) · 실제 인증/송금·실기기 미검증 | [#20](https://github.com/team-bestfriend/danjjak-app/issues/20), [#34](https://github.com/team-bestfriend/danjjak-app/issues/34), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-011 | [문구·가족 음성 편집](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#30](https://github.com/team-bestfriend/danjjak-app/issues/30), [#25](https://github.com/team-bestfriend/danjjak-app/issues/25), [#38](https://github.com/team-bestfriend/danjjak-app/issues/38), [#40](https://github.com/team-bestfriend/danjjak-app/issues/40), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-012 | [분석·문구 개선](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#29](https://github.com/team-bestfriend/danjjak-app/issues/29), [#24](https://github.com/team-bestfriend/danjjak-app/issues/24), [#38](https://github.com/team-bestfriend/danjjak-app/issues/38), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-013 | [번호·업무 음성 입력](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#31](https://github.com/team-bestfriend/danjjak-app/issues/31), [#40](https://github.com/team-bestfriend/danjjak-app/issues/40), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-014 | [접근성·세션·새로고침](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#15](https://github.com/team-bestfriend/danjjak-app/issues/15), [#14](https://github.com/team-bestfriend/danjjak-app/issues/14), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-015 | [금융·기록 종료 경계](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#28](https://github.com/team-bestfriend/danjjak-app/issues/28), [#11](https://github.com/team-bestfriend/danjjak-app/issues/11), [#24](https://github.com/team-bestfriend/danjjak-app/issues/24), [#36](https://github.com/team-bestfriend/danjjak-app/issues/36), [#37](https://github.com/team-bestfriend/danjjak-app/issues/37), [#40](https://github.com/team-bestfriend/danjjak-app/issues/40), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-016 | [모의 계좌 불러오기](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#19](https://github.com/team-bestfriend/danjjak-app/issues/19), [#13](https://github.com/team-bestfriend/danjjak-app/issues/13), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-017 | [사람별 복수 계좌](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#21](https://github.com/team-bestfriend/danjjak-app/issues/21), [#11](https://github.com/team-bestfriend/danjjak-app/issues/11), [#13](https://github.com/team-bestfriend/danjjak-app/issues/13), [#22](https://github.com/team-bestfriend/danjjak-app/issues/22), [#26](https://github.com/team-bestfriend/danjjak-app/issues/26), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |
| SC-018 | [문구·금액·음성 영역](../specs/requirements/validation-scenarios.md) | 본선 검증 계획 · 미실행 | [#27](https://github.com/team-bestfriend/danjjak-app/issues/27), [#14](https://github.com/team-bestfriend/danjjak-app/issues/14), [#18](https://github.com/team-bestfriend/danjjak-app/issues/18), [#32](https://github.com/team-bestfriend/danjjak-app/issues/32), [#35](https://github.com/team-bestfriend/danjjak-app/issues/35), [#41](https://github.com/team-bestfriend/danjjak-app/issues/41) |

## CH

| ID | 기준 명세·항목 | 상태 | 실제 이슈: 주 담당 → 연계 |
| --- | --- | --- | --- |
| CH-01 | [분석 로그형 문구 → 쉬운 사실·행동 안내](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#38](https://github.com/team-bestfriend/danjjak-app/issues/38), [#29](https://github.com/team-bestfriend/danjjak-app/issues/29) |
| CH-02 | [많이 쓴 패턴 위로 → 완료 횟수 내림차순](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#29](https://github.com/team-bestfriend/danjjak-app/issues/29), [#23](https://github.com/team-bestfriend/danjjak-app/issues/23) |
| CH-03 | [TTS 톤 → 차분·명료한 한국어](../specs/requirements/design-decisions.md) | 목표 반영·음색 선택 · 구현 상태와 별개 | [#27](https://github.com/team-bestfriend/danjjak-app/issues/27) |
| CH-04 | [iPhone 12 Pro Max → 428×926·큰 글씨·키보드](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#14](https://github.com/team-bestfriend/danjjak-app/issues/14) |
| CH-05 | [번호만 말하기 → 번호 매칭·기존 확인창](../specs/requirements/design-decisions.md) | 확정 · 구현 상태와 별개 | [#31](https://github.com/team-bestfriend/danjjak-app/issues/31), [#26](https://github.com/team-bestfriend/danjjak-app/issues/26) |
| CH-06 | [음성 설명 축소 → 짧은 예시·도움말](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#18](https://github.com/team-bestfriend/danjjak-app/issues/18) |
| CH-07 | [업무 내용 크게 → 홈·목록·확인창 공통](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#23](https://github.com/team-bestfriend/danjjak-app/issues/23), [#14](https://github.com/team-bestfriend/danjjak-app/issues/14) |
| CH-08 | [TTS 호칭 → AI 음성, 단짝이 별칭 보류](../specs/requirements/design-decisions.md) | 설계안 · 구현 상태와 별개 | [#27](https://github.com/team-bestfriend/danjjak-app/issues/27) |
| CH-09 | [설명·단계 음성 편집 → 패턴 수정에 통합](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#25](https://github.com/team-bestfriend/danjjak-app/issues/25) |
| CH-10 | [폰트·아이콘 → 일관된 체계·이모티콘 정리](../specs/requirements/design-decisions.md) | 원칙 반영·자산 선택 · 구현 상태와 별개 | [#14](https://github.com/team-bestfriend/danjjak-app/issues/14) |
| CH-11 | [분석 목적 → 업무·단계 확인 후 안내 개선](../specs/requirements/design-decisions.md) | 설계안 · 구현 상태와 별개 | [#29](https://github.com/team-bestfriend/danjjak-app/issues/29), [#38](https://github.com/team-bestfriend/danjjak-app/issues/38) |
| CH-12 | [이름 아래 ‘단짝 시연 사용자’ 삭제](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#18](https://github.com/team-bestfriend/danjjak-app/issues/18), [#17](https://github.com/team-bestfriend/danjjak-app/issues/17) |
| CH-13 | [생성/수정 시 미리 생성 → 저장 후 준비·갱신](../specs/requirements/design-decisions.md) | 선택 검토 · 미채택 | [#35](https://github.com/team-bestfriend/danjjak-app/issues/35) |
| CH-14 | [상·하단 버튼 확대 → 최소 48×48 CSS px](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#14](https://github.com/team-bestfriend/danjjak-app/issues/14) |
| CH-15 | [금액 읽기 → 쉼표·원·한글 병기](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#32](https://github.com/team-bestfriend/danjjak-app/issues/32) |
| CH-16 | [카톡 문구 → 보호자 표시·본인 시연 안내](../specs/requirements/design-decisions.md) | 확정 · 구현 상태와 별개 | [#39](https://github.com/team-bestfriend/danjjak-app/issues/39) |
| CH-17 | [온보딩 → 단축번호·가족 안내·이상 거래](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#18](https://github.com/team-bestfriend/danjjak-app/issues/18), [#17](https://github.com/team-bestfriend/danjjak-app/issues/17) |
| CH-18 | [상시 음성/대본 축소 → 상태·다시 듣기·펼치기](../specs/requirements/design-decisions.md) | 설계안 · 구현 상태와 별개 | [#27](https://github.com/team-bestfriend/danjjak-app/issues/27) |
| CH-19 | [직접 송금 분리 → 입력별 화면](../specs/requirements/design-decisions.md) | 분리 반영·순서 설계안 · 구현 상태와 별개 | [#36](https://github.com/team-bestfriend/danjjak-app/issues/36) |
| CH-20 | [쉬운 용어·단계명·‘‹ 뒤로’](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#14](https://github.com/team-bestfriend/danjjak-app/issues/14) |
| CH-21 | [확인·인증·완료 → 금액 CTA·본인 확인·구체적 결과](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#32](https://github.com/team-bestfriend/danjjak-app/issues/32) |
| CH-22 | [경고 문구 → 쉬운 제목·실제 고액/반복 사유](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#37](https://github.com/team-bestfriend/danjjak-app/issues/37) |
| CH-23 | [은행 로고 → 로고+이름·대체 표시](../specs/requirements/design-decisions.md) | 원칙 반영·자산 선택 · 구현 상태와 별개 | [#14](https://github.com/team-bestfriend/danjjak-app/issues/14) |
| CH-24 | [계좌 화살표 → 체크·선택됨](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#21](https://github.com/team-bestfriend/danjjak-app/issues/21), [#14](https://github.com/team-bestfriend/danjjak-app/issues/14) |
| CH-25 | [두 탭/보호자 분리 의견 → 네 탭 유지](../specs/requirements/design-decisions.md) | 확정 · 구현 상태와 별개 | [#14](https://github.com/team-bestfriend/danjjak-app/issues/14) |
| CH-26 | [단계 UI 통일 → 질문·입력·다음·음성 배치](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#14](https://github.com/team-bestfriend/danjjak-app/issues/14) |
| CH-27 | [복수 계좌·단일 확인 → 모두 지원](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#21](https://github.com/team-bestfriend/danjjak-app/issues/21), [#26](https://github.com/team-bestfriend/danjjak-app/issues/26), [#32](https://github.com/team-bestfriend/danjjak-app/issues/32) |
| CH-28 | [아들 송금 1→3 → 확인 유지·연속 순번](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#26](https://github.com/team-bestfriend/danjjak-app/issues/26), [#23](https://github.com/team-bestfriend/danjjak-app/issues/23) |
| CH-29 | [틀린 비밀번호 → 오류 문구·음성·입력 초기화](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#32](https://github.com/team-bestfriend/danjjak-app/issues/32), [#27](https://github.com/team-bestfriend/danjjak-app/issues/27) |
| CH-30 | [점선 추가 버튼 → 일반 버튼](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#21](https://github.com/team-bestfriend/danjjak-app/issues/21) |
| CH-31 | [기존 사람에게 계좌 추가](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#21](https://github.com/team-bestfriend/danjjak-app/issues/21) |
| CH-32 | [FDS UI → 이유·대상·금액·결정, TTS 표식 제거](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#37](https://github.com/team-bestfriend/danjjak-app/issues/37) |
| CH-33 | [로그인 후 계좌 불러오기 → 모의 후보·추가 화면](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#19](https://github.com/team-bestfriend/danjjak-app/issues/19) |
| CH-34 | [홈 작은 안내 제거 → 이용방법·도움말](../specs/requirements/design-decisions.md) | 반영 · 구현 상태와 별개 | [#18](https://github.com/team-bestfriend/danjjak-app/issues/18) |

## D

| ID | 기준 명세·항목 | 상태 | 실제 이슈: 주 담당 → 연계 |
| --- | --- | --- | --- |
| D-01 | [질문 유지 + 안내 중·멈춤·다시 듣기·안내 보기](../specs/requirements/design-decisions.md) | 설계안 · 미확정 | [#27](https://github.com/team-bestfriend/danjjak-app/issues/27) |
| D-02 | [많이 쓴 업무·안내 검토 단계 → 문구·가족 음성 개선](../specs/requirements/design-decisions.md) | 설계안 · 미확정 | [#29](https://github.com/team-bestfriend/danjjak-app/issues/29) |
| D-03 | [저장 후 AI 음성 생성·재사용](../specs/requirements/design-decisions.md) | 선택 검토 · 미채택 | [#35](https://github.com/team-bestfriend/danjjak-app/issues/35) |
| D-04 | [AI 음성 호칭·일관된 글꼴/아이콘·로고+은행명](../specs/requirements/design-decisions.md) | 설계안 · 미확정 | [#14](https://github.com/team-bestfriend/danjjak-app/issues/14), [#27](https://github.com/team-bestfriend/danjjak-app/issues/27) |
| D-05 | [내 계좌→이름→은행→번호→계좌 확인→금액→확인→PIN](../specs/requirements/design-decisions.md) | 설계안 · 미확정 | [#36](https://github.com/team-bestfriend/danjjak-app/issues/36) |
