<div align="center">

# 단짝 (Danjjak)

**가족이 도와드리는 똑똑한 금융 생활**

고령 사용자가 자주 이용하는 금융 업무를 단축번호와 음성 안내로 쉽게 수행하는 해커톤 MVP

[![Vue](https://img.shields.io/badge/Vue-3-42b883)](frontend/README.md)
[![Spring Framework](https://img.shields.io/badge/Spring%20Framework-5.3-6db33f)](backend/README.md)
[![Java](https://img.shields.io/badge/Java-17-orange)](backend/README.md)
[![Tomcat](https://img.shields.io/badge/Tomcat-9-f8dc75)](backend/README.md)
[![OpenAPI](https://img.shields.io/badge/API-OpenAPI-6ba539)](contracts/README.md)

KB IT's Your Life 해커톤

</div>

---

## 📝 프로젝트 소개

- 자주 하는 금융 업무를 번호로 찾고 확인한 뒤 시작
- 큰 글씨·쉬운 질문·AI 음성·가족 녹음으로 단계별 안내
- 모의 계좌와 거래 데이터로 조회·송금 흐름 시연
- 고액·반복 송금을 한 번 더 확인하는 규칙 기반 이상거래 탐지
- 동의한 이용 기록으로 자주 쓰는 업무와 안내를 검토할 단계 확인

**현재는 명세·설계·개발 이슈를 준비한 단계입니다.** 아래 기술과 기능은 본선 개발 범위이며,
Java 파일은 한국어 설계 설명과 빈 선언만 포함합니다. 실행 가능한 앱과 기능 검증 결과는 아직 없습니다.

## ✨ MVP 개발 범위

### 🔑 인증·설정·계좌 준비

- 카카오 로그인·세션 복구·로그아웃
- 이용 기록과 보호자 알림을 각각 선택하는 동의
- 글씨 크기·안내 속도·기본 음성 설정
- 모의 내 계좌 불러오기와 사람별 복수 받는 계좌 관리

### 🔢 금융 단축번호

- 기본 8개 업무·7종 템플릿, 최대 12개 활성 번호
- 패턴 등록·수정·번호 이동·교환 확인·비활성화
- 카드 선택이나 번호·정해진 업무 음성 입력 후 실행 전 확인
- 홈 / 단축번호 / 이용 분석 / 설정의 네 탭

### 💸 안내형 송금·금융 조회

- 내 계좌 → 받는 사람 → 받는 계좌 → 금액 → 최종 확인 → 본인 확인
- 받는 계좌가 하나여도 명시적 확인, 직접 입력은 한 화면 한 입력
- 숫자·한글 금액 병기와 모의 계좌 비밀번호 4자리 확인
- 잔액 차감과 거래 생성을 함께 확정하는 모의 송금
- 잔액·거래내역·연금·관리비·공과금 조회와 고객센터 전화

### 🔊 음성·단계 안내

- 현재 질문을 유지하는 화면 안내와 AI 음성·다시 듣기
- 패턴 수정 안에서 시작 설명·실제 단계별 문구와 음성 편집
- 실제 마이크로 가족 안내 녹음·저장·대상별 교체
- 가족 녹음이 없거나 재생에 실패하면 같은 문구의 AI 음성으로 대체
- 문구 변경과 녹음 불일치 안내, 해당 단계 재녹음 연결

### 🚨 이상거래·보호자 대응

- 패턴 송금과 직접 송금에 같은 서버 판정 적용
- 고액·최근 완료 송금 반복 여부에 따른 주의·높은 주의 안내
- 전체 사유·받는 계좌·금액 확인 후 다시 확인·계속·취소
- 저장된 보호자 번호 확인 후 전화 앱 연결
- 높은 주의·동의·명시적 선택 시 카카오 알림 시연

‘보호자에게 카톡 보내기’ 버튼의 실제 시연 수신자는 **로그인한 본인 계정의 ‘나에게 보내기’**입니다.
실제 전송과 모의 결과를 구분하며, 보호자 원격 승인 기능은 포함하지 않습니다.

### 📊 이용 분석·안내 개선

- 이용 기록 동의가 있는 패턴 실행·단계 방문만 기록
- 재시도·뒤로·잘못 누름·경로 이탈 합계와 방문 시간 집계
- 기간별 완료 횟수와 안내 검토 단계 표시
- 현재·제안 문구 비교 후 명시적 적용, 같은 단계의 가족 음성 편집 연결

상세 규칙은 [기능 명세](docs/specs/requirements.md)를 따릅니다.
AI 음성 미리 생성은 [선택 검토 이슈](https://github.com/team-bestfriend/danjjak-app/issues/35)로 분리합니다.

## 🧭 주요 시연 흐름

본선 구현 후 확인할 목표 흐름입니다. 완료 조건은 [검증 시나리오](docs/specs/requirements/validation-scenarios.md)에 정리했습니다.

### 첫 이용

```text
서비스 소개 → 카카오 로그인 → 선택 동의 → 모의 내 계좌 불러오기 → 홈
```

### 정상 송금

```text
카드·번호 음성 선택 → 실행 전 확인 → 시작하기 → 송금 단계 → 본인 확인 → 완료
```

### 이상거래 확인

```text
송금 제출 → 서버 FDS 판정 → 사유·대상·금액 확인 → 재확인·연락 → 계속 또는 취소
```

### 안내 개선

```text
이용 분석 → 안내 검토 단계 → 현재·제안 비교 → 명시적 적용 → 같은 단계 재녹음
```

## 🛠 기술 스택

단짝의 기능별 패키지와 계층을 기준으로 개발합니다.

| 영역 | 기술 |
| --- | --- |
| 프론트엔드 | Vue 3 · Vite · JavaScript · Pinia · Vue Router |
| 백엔드 | Java 17 · Spring Framework 5.3 · Spring MVC · MyBatis |
| 실행 환경 | Gradle WAR · Apache Tomcat 9 |
| 데이터베이스 | MySQL 8.4 · Flyway |
| 로컬 환경 | Docker Compose 구성 예정 |
| HTTP 계약 | OpenAPI · Redocly 검증 |
| 로그 | Log4j2 |

## 🏗 시스템 구조

```text
Vue 프론트엔드
    → Tomcat 9
        → Spring MVC controller
            → service
                → MyBatis mapper
                    → MySQL
```

- 기본 패키지: `com.bestfriend.danjjak`
- 기능별 `controller → service → mapper`, `dto`, `model` 구조 유지
- AI 합성·카카오 메시지·가족 파일 저장 책임을 기능별 서비스에서 분리
- [백엔드 설계](docs/design/backend-design.md) · [Java 설계 파일 목록](docs/design/backend-inventory.md)

## 📐 데이터 관계·ERD

- 사용자 → 내 계좌·등록 사람·패턴
- 등록 사람 → 여러 받는 계좌, 송금 패턴 → 특정 받는 계좌
- 패턴 → 시작 안내·순서 있는 단계 → 대상별 문구·음성
- 패턴 → 동의한 실행 → 단계 방문, 완료 송금 → 거래
- 이상거래 판정 → 사용자 결정 → 계속한 경우 거래 연결

물리 스키마와 최종 ERD는 본선에 확정합니다.
[개념 데이터 모델](docs/design/data-model.md)과 [아키텍처](docs/specs/requirements/architecture.md)를 참고합니다.

## 🚀 시작하기

```powershell
git clone https://github.com/team-bestfriend/danjjak-app.git
cd danjjak-app
```

현재는 설계 문서와 배정 이슈부터 확인합니다. 실행 환경·패키지 설정·SQL·구체 HTTP 계약은
본선의 해당 이슈에서 작성하므로, 지금 실행할 빌드·DB 시작 명령은 제공하지 않습니다.

| 항목 | 안내 |
| --- | --- |
| 기능·화면·완료 조건 | [요구사항 목차](docs/specs/requirements.md) |
| 개발 순서·선행 작업 | [Epic·구현 이슈 지도](docs/issues/issue-map.md) |
| 프론트엔드 | [frontend/README.md](frontend/README.md) |
| 백엔드 | [backend/README.md](backend/README.md) |
| 데이터베이스 | [db/README.md](db/README.md) |
| 로컬 실행 환경 | [infra/README.md](infra/README.md) |
| HTTP 계약 | [contracts/README.md](contracts/README.md) |

## 📁 프로젝트 구조

```text
danjjak-app/
|-- frontend/              프론트엔드 개발 안내
|-- backend/               백엔드 설계 선언·개발 안내
|-- contracts/             HTTP 계약 준비 문서
|-- db/                    데이터베이스 개발 안내
|-- infra/                 실행 환경 준비 문서
|-- docs/
|   |-- specs/             기능·화면·검증 명세
|   |-- design/            구조·데이터 관계·남은 결정
|   `-- issues/            이슈 작성 지침·요구사항 추적
|-- .github/               이슈·PR 템플릿
|-- .agents/skills/        프로젝트 Agent 스킬
|-- AGENTS.md              Agent 라우팅·공통 규칙
|-- CONTRIBUTING.md        협업 규칙
`-- README.md
```

## 🌐 API 명세

- 현재 기준: [기능별 입출력 의미](docs/specs/requirements/architecture.md)와 [계약 준비 문서](contracts/README.md)
- 본선 작업: [공동 HTTP 계약과 데이터·종료 책임 확정](https://github.com/team-bestfriend/danjjak-app/issues/11)
- 인증·설정·사람/계좌·패턴/안내·조회/송금·FDS/알림·기록/분석의 구체 계약 작성 예정

## 🤝 협업·개발 이슈

- Epic → 구현 하위 이슈 → 내부 체크리스트 구조로 개발합니다.
- [Epic 10개·구현 30개·선택 검토 1개](docs/issues/issue-map.md)와 [전체 요구사항 추적표](docs/issues/requirement-map.md)
- [기여 규칙](CONTRIBUTING.md) · [Agent 지침](AGENTS.md) · [Claude 지침](CLAUDE.md) · [남은 설계 결정](docs/design/open-decisions.md)
- [개발 준비 현황](docs/development-status.md) · [설계 검증](docs/design-validation.md)

## 👥 팀

[team-bestfriend](https://github.com/team-bestfriend) · KB IT's Your Life 해커톤

---

<div align="center">

**단짝 (Danjjak)** · 가족이 도와드리는 똑똑한 금융 생활

<sub>모의 금융 데이터로 시연하는 프로젝트입니다. 실제 금융망·마이데이터와 연결하지 않습니다.</sub>

</div>
