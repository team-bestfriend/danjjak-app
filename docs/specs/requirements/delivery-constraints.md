# 개발 범위·순서

[목차](../requirements.md)

## 사전·본선 구분

| 사전 준비 | 본선 시작 후 |
| --- | --- |
| 아이디어·목적·흐름 | 화면·UI 구현 |
| 화면 구성·문구·시안 | 프론트엔드·백엔드 코드 |
| 기술·외부 API 검토 | 로그인·AI 음성·카카오 실제 연동 |
| 데이터 항목·관계·모의 자료 계획 | DB 구축·초기 자료 입력·연동 |
| 구성·책임·입출력 의미 | 구체적인 HTTP 계약·구현 |
| 환경 준비 목록·작업 순서 | 테스트·배포·발표자료 |

- 기준: 제공된 대회 안내 캡처.
- 문서 산출물: 기획·설계·검토·당일 완료 조건.
- 실행용 초기화 자료·DB 스크립트·미리 완성한 음성 결과물 제외.

## 비기능 요구사항

| ID | 항목 | 완료 조건 |
| --- | --- | --- |
| NFR-001 | 기술 | Vue 3·Vite·Pinia·Vue Router / Java 17·Spring Framework 5.x·MyBatis |
| NFR-002 | 저장 | MySQL, 구조·모의 자료 버전 관리, Flyway 변경 순서 |
| NFR-003 | 실행 재현 | 당일 안내로 DB·BE·FE 시작, 핵심 흐름 재현 |
| NFR-004 | HTTP 계약 | OpenAPI로 FE·BE 필드 계약 통일 |
| NFR-005 | 접근성 | 쉬운 문구·글씨·대비·조작 영역·상태 피드백 |
| NFR-006 | 민감 정보 | 업무 테이블에 원문 PIN·OAuth 토큰·음성 본문·실금융 자격정보 저장 없음 |
| NFR-007 | 반복 시연 | 준비된 세션에서 음성·카톡 실패에도 모의 금융 진행 |
| NFR-008 | 규모 | 이번 기능에 필요한 구성만 구현 |
| NFR-009 | 검증 | 관련 빌드·BE 검증·계약 확인·인수 시나리오 |
| NFR-010 | 외부 서비스 | 음성·카톡 실패가 잔액·거래·판정 상태에 영향 없음 |
| NFR-011 | 로그 | PIN·토큰·전체 계좌·인식 문자열·녹음 내용 제외 |
| NFR-012 | 화면 | iPhone 12 Pro Max·큰 글씨·키보드·팝업·음성 제어 검증 |

## 당일 순서

| 순서 | 작업 | 완료물 |
| --- | --- | --- |
| 1 | 공통 화면·계약·데이터 설계 | 단계·상태·OpenAPI·데이터 구조 |
| 2 | 환경·로그인·동의·모의 계좌 | 첫 이용 → 홈 |
| 3 | 사람·복수 계좌·단축번호 | 기본 업무·관리·확인창 |
| 4 | 조회·송금·FDS | 정상·주의·높은 주의·취소 |
| 5 | AI/가족 안내·번호 입력 | 패턴 내 음성 편집·번호 확인창 |
| 6 | 기록·분석·안내 개선 | 정확한 집계·비교·적용 |
| 7 | 통합·기기 검증·시연 | 검증 결과·배포·발표자료 |

## 우선순위

| 구분 | 항목 |
| --- | --- |
| 필수 기능 | 로그인·계좌·4탭·패턴·조회·송금·FDS·AI/가족 안내·번호 입력·분석 |
| 필수 개선 | 큰 조작·쉬운 문구·업무 강조·금액 병기·단계 분리·오류 음성·패턴 내 편집 |
| 화면 설계안 | D-01 음성 영역, D-02 분석, D-04 호칭/자산, D-05 직접 송금 순서 |
| 선택 기술 | D-03 음성 미리 생성 |

## 외부 연계·실패

| 연계 | 실패 시 |
| --- | --- |
| 카카오 로그인 | 재시도·취소. 로그인 성공으로 가장 금지 |
| 모의 계좌 | 재시도, 준비 전 금융 제한 |
| AI 음성 | 화면 안내·다시 듣기 |
| 실제 녹음 | 권한/지원 안내·AI 선택 |
| 번호 인식 | 수동 카드 선택 |
| 카톡 | 실제 전송·모의 결과 구분 |
| 전화 | 번호 표시 |

## Agent Notes

- These are plans, not completion evidence. Pre-event design does not authorize executing scenarios, migrations, integrations, or deployment.
- Keep technical changes within the requested demo scope. Record contract/environment impact when changing the chosen stack.
- Use mock PIN comparison data and file references; do not add production banking, guardian apps, ML models, or unnecessary infrastructure.
- Define API fields and physical schemas during the event. Keep FE/BE behavior aligned with the contract.
- The work order expresses dependencies, not fixed hours or staffing. Parallel team work can start after shared contracts are agreed.
- Do not let optional pre-generation delay core flows. D entries are proposals, not extra approval gates.
- Validate loading, empty, invalid, failed, and cancelled states; refetch saved data.
- Verify agreement among transfer, balance, transaction, execution, and analytics.
- Record actual-provider tests, substituted responses, and untested cases separately. Never claim real bank/MyData linkage.
- Apply [related SC scenarios](validation-scenarios.md); report success only after execution.
