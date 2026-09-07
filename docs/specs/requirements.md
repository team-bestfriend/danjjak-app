# 단짝 해커톤 기능명세서

| 항목 | 내용 |
| --- | --- |
| 기준일 | 2026-09-07 |
| 목적 | 본선 당일 개발할 기능·화면·구조·완료 조건 |
| 범위 | 고령 사용자를 위한 모의 금융 서비스 |
| 기준 화면 | iPhone 12 Pro Max 세로 |
| 문서 언어 | 기능·화면·결정: 한국어 / Agent 작업 지침: 영어 |
| 준비 단계 | 사전: 기획·설계·기술 검토 / 본선: 구현·연동·검증·배포 |
| 상태 | 목표 명세. 구현 완료나 대회 반입 승인을 뜻하지 않음 |

## 확정 사항

| 항목 | 결정 |
| --- | --- |
| 하단 탭 | 홈 / 단축번호 / 이용 분석 / 설정 |
| 번호 음성 | 번호 인식 → 실행 전 확인창 → 시작하기 |
| 카톡 시연 | 버튼: ‘보호자에게 카톡 보내기’ / 실제: 본인 계정 ‘나에게 보내기’ |
| 받는 계좌 | 사람별 복수 계좌. 한 개여도 확인 단계 유지 |
| 화면 | 업무 이름 우선, 큰 조작 영역, 쉬운 문구, 숫자·한글 금액 |
| 음성 편집 | 패턴 수정 안에서 설명·단계별 문구와 음성 편집 |
| 계좌 준비 | 로그인 후 모의 계좌 불러오기. 실제 은행·마이데이터 연동 제외 |

## 문서 목록

| 문서 | 요구사항 |
| --- | --- |
| [제품 범위](requirements/product-scope.md) | 목적·역할·제외 범위 |
| [공통 UX](requirements/shared-ux.md) | UX-001–014 |
| [아키텍처](requirements/architecture.md) | 구성·책임·데이터 관계 |
| [인증·설정](requirements/auth-settings.md) | FR-001–004, 053, 057–058, 060 |
| [사람·계좌](requirements/people-accounts.md) | FR-005–009 |
| [단축번호·패턴](requirements/shortcuts-patterns.md) | FR-010–017 |
| [음성·단계 안내](requirements/guidance-voice.md) | FR-018–027, 054–056, 061 |
| [모의 송금](requirements/mock-transfer.md) | FR-028–031, 059 |
| [금융 조회](requirements/financial-inquiries.md) | FR-032–034 |
| [이상거래·보호자 대응](requirements/fds-guardian.md) | FR-035–043 |
| [이용 분석](requirements/usage-analysis.md) | FR-044–052 |
| [개발 범위·순서](requirements/delivery-constraints.md) | NFR-001–012 |
| [검증 시나리오](requirements/validation-scenarios.md) | SC-001–018 |
| [수정사항·결정](requirements/design-decisions.md) | CH-01–34, D-01–05 |

## Agent Guide

- Start with AGENTS.md and the assigned issue. Use this index to locate only missing context; read the linked feature rules and relevant CH/D/SC entries.
- Read shared UX for UI changes and architecture/delivery constraints for affected BE/API/DB boundaries; follow only necessary cross-feature links.
- Priority: current user decisions → revised specs → design comments. A proposal, optional feature, or scenario is not completed work.
- Preserve IDs and business rules. Keep each rule in its owning document; link instead of duplicating. Update affected scenarios when behavior changes.
- Use Korean for product rules, UI copy, and human-facing design descriptions; English for agent workflow and implementation instructions. Do not duplicate both languages.
- Pre-event work follows the design scope. Define concrete HTTP fields and physical schemas during the event.
- Use this index only to locate context missing from the assigned issue; follow AGENTS.md for task routing.
