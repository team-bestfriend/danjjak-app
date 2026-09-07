# 수정사항·결정

[목차](../requirements.md)

| 상태 | 의미 |
| --- | --- |
| 확정 | 사용자 답변으로 결정 |
| 반영 | 목표 명세에 반영, 구현 완료 의미 아님 |
| 설계안 | 화면·표현의 기본 제안 |
| 선택 검토 | 검증 후 채택 여부 결정 |

## 사용자 확정

| 항목 | 결정 |
| --- | --- |
| 번호 음성 | 기존 실행 전 확인창 → 시작하기 |
| 카톡 | ‘보호자에게 카톡 보내기’ 표시, 실제 본인 ‘나에게 보내기’ |
| 탭 | 홈·단축번호·이용 분석·설정 유지 |

## 수정사항 대응

| ID | 의견 → 반영 | 관련 항목 | 상태 |
| --- | --- | --- | --- |
| CH-01 | 분석 로그형 문구 → 쉬운 사실·행동 안내 | FR-048–050, [분석](usage-analysis.md) | 반영 |
| CH-02 | 많이 쓴 패턴 위로 → 완료 횟수 내림차순 | FR-048 | 반영 |
| CH-03 | TTS 톤 → 차분·명료한 한국어 | FR-022, [음성](guidance-voice.md) | 목표 반영·음색 선택 |
| CH-04 | iPhone 12 Pro Max → 428×926·큰 글씨·키보드 | NFR-012, [UX](shared-ux.md) | 반영 |
| CH-05 | 번호만 말하기 → 번호 매칭·기존 확인창 | FR-027·016 | 확정 |
| CH-06 | 음성 설명 축소 → 짧은 예시·도움말 | FR-060 | 반영 |
| CH-07 | 업무 내용 크게 → 홈·목록·확인창 공통 | UX-011, [패턴](shortcuts-patterns.md) | 반영 |
| CH-08 | TTS 호칭 → AI 음성, 단짝이 별칭 보류 | D-04 | 설계안 |
| CH-09 | 설명·단계 음성 편집 → 패턴 수정에 통합 | FR-054–056 | 반영 |
| CH-10 | 폰트·아이콘 → 일관된 체계·이모티콘 정리 | D-04 | 원칙 반영·자산 선택 |
| CH-11 | 분석 목적 → 업무·단계 확인 후 안내 개선 | FR-048–052, D-02 | 설계안 |
| CH-12 | 이름 아래 ‘단짝 시연 사용자’ 삭제 | FR-003, [설정](auth-settings.md) | 반영 |
| CH-13 | 생성/수정 시 미리 생성 → 저장 후 준비·갱신 | FR-061, D-03 | 선택 검토 |
| CH-14 | 상·하단 버튼 확대 → 최소 48×48 CSS px | UX-011 | 반영 |
| CH-15 | 금액 읽기 → 쉼표·원·한글 병기 | FR-059, [송금](mock-transfer.md) | 반영 |
| CH-16 | 카톡 문구 → 보호자 표시·본인 시연 안내 | FR-042, [FDS](fds-guardian.md) | 확정 |
| CH-17 | 온보딩 → 단축번호·가족 안내·이상 거래 | FR-001 | 반영 |
| CH-18 | 상시 음성/대본 축소 → 상태·다시 듣기·펼치기 | FR-021, D-01 | 설계안 |
| CH-19 | 직접 송금 분리 → 입력별 화면 | FR-029, D-05 | 분리 반영·순서 설계안 |
| CH-20 | 쉬운 용어·단계명·‘‹ 뒤로’ | UX-003·007 | 반영 |
| CH-21 | 확인·인증·완료 → 금액 CTA·본인 확인·구체적 결과 | FR-030–031 | 반영 |
| CH-22 | 경고 문구 → 쉬운 제목·실제 고액/반복 사유 | FR-039 | 반영 |
| CH-23 | 은행 로고 → 로고+이름·대체 표시 | D-04 | 원칙 반영·자산 선택 |
| CH-24 | 계좌 화살표 → 체크·선택됨 | UX-011, [계좌](people-accounts.md) | 반영 |
| CH-25 | 두 탭/보호자 분리 의견 → 네 탭 유지 | 공통 UX | 확정 |
| CH-26 | 단계 UI 통일 → 질문·입력·다음·음성 배치 | UX-003·011–012 | 반영 |
| CH-27 | 복수 계좌·단일 확인 → 모두 지원 | FR-006·014·028 | 반영 |
| CH-28 | 아들 송금 1→3 → 확인 유지·연속 순번 | FR-017 | 반영 |
| CH-29 | 틀린 비밀번호 → 오류 문구·음성·입력 초기화 | FR-030·022 | 반영 |
| CH-30 | 점선 추가 버튼 → 일반 버튼 | FR-006 | 반영 |
| CH-31 | 기존 사람에게 계좌 추가 | FR-006 | 반영 |
| CH-32 | FDS UI → 이유·대상·금액·결정, TTS 표식 제거 | FR-039, UX-012 | 반영 |
| CH-33 | 로그인 후 계좌 불러오기 → 모의 후보·추가 화면 | FR-058 | 반영 |
| CH-34 | 홈 작은 안내 제거 → 이용방법·도움말 | FR-060 | 반영 |

## 남은 선택

| ID | 기본안 | 결정 기준 |
| --- | --- | --- |
| D-01 | 질문 유지 + 안내 중·멈춤·다시 듣기·안내 보기 | 큰 글씨·음성 없이 진행 가능. 시간 경과 자동 표시는 별도 검토 |
| D-02 | 많이 쓴 업무·안내 검토 단계 → 문구·가족 음성 개선 | 팀의 활용 목적·지표 이해 확인 |
| D-03 | 저장 후 AI 음성 생성·재사용 | 당일 지연·실패·변경 무효화 검증 |
| D-04 | AI 음성 호칭·일관된 글꼴/아이콘·로고+은행명 | 한국어 청취·가독성·사용 가능 자산 |
| D-05 | 내 계좌→이름→은행→번호→계좌 확인→금액→확인→PIN | 한 번에 한 입력, 단계 부담·뒤로·큰 글씨 검토 |

- 고정 조건: 네 탭, 번호 확인창, 카톡 표시/본인 시연, 직접 입력 분리, 단일 계좌 확인, 금액 병기.
- 미제공 자료: 메모의 피그마·참고 UI·image.png 실제 파일/링크. 구체 배치·자산은 미확인.

## 기술 검토

| 주제 | 확인 내용·설계 의미 |
| --- | --- |
| AI 음성 | 공식 문서의 톤·속도 지시와 스트리밍 지원 → 톤·초기 지연 검토 근거 |
| 미리 생성 | 앱의 저장·재사용 설계안. 제공사의 자동 저장 정책·성능 보장 아님 |
| 합성 음성 안내 | 설정·최초 안내에서 AI 음성임을 설명 |
| 카카오 | 나에게 보내기 수신자는 로그인한 본인. 친구 전송은 별도 대상·권한 |

음성 근거: [OpenAI 공식 문서](https://developers.openai.com/api/docs/guides/text-to-speech). 카톡 근거: [카카오 메시지 공식 문서](https://developers.kakao.com/docs/ko/kakaotalk-message/rest-api).

| 비교 | 즉시 생성 | 미리 생성 |
| --- | --- | --- |
| 재생 시작 | 요청 시 생성 대기 | 준비 파일 재사용 |
| 변경 | 현재 문구 요청 | 문구·방식·속도·설정 변경 시 갱신 |
| 실패 | 화면 안내·재시도 | 저장·금융 업무와 생성 실패 분리 |
| 관리 | 요청·중단 | 준비 상태·파일·오래된 결과 제외 |

## Agent Notes

- Keep CH/D status distinct from implementation status. Do not turn unresolved proposals into user-confirmed decisions.
- Validate D-03 during the event on the same device/network: short/long scripts, cold/prepared playback, edits, speed changes, and failures.
- Measure button-to-first-audio latency. Prepared playback within 1 second is a proposed comparison target, not a measured result or SLA.
- Adopt only with useful latency improvement and correct invalidation/failure handling. Otherwise keep on-demand synthesis and readable guidance.
- Check these integration boundaries: actual selected source, single financial/log finalization, swap confirmation for all gestures, inactive history, truthful recent-use time, refresh recovery, and valid defaults for every guidance target.
- Scenario coverage: [validation](validation-scenarios.md). Preserve new requirements over stale implementation references.
