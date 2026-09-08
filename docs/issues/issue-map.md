# 개발 이슈 매핑

- 초기 범위는 Epic **10개**, 확정 구현 **30개**, 선택 검토 **1개**: 총 **41개**입니다. #11의 계약 준비를 완료했으며 이후 추가된 [FE 이관 #42](https://github.com/team-bestfriend/danjjak-app/issues/42)는 별도로 진행합니다.
- 네이티브 상위/하위 관계 **31개**, 차단 관계 **56개**. 아래 선행 이슈는 해당 결과의 구현·검증 준비 조건입니다.
- 필수 경로에는 선택 검토를 넣지 않았습니다. 상세 규칙은 명세, 작업 범위·완료 조건은 실제 이슈를 기준으로 읽습니다.
- [144개 ID 추적표](requirement-map.md) · [이슈 작성 지침](guide.md) · [검증 결과](../design-validation.md)

## 기능별 Epic

| Epic | 확정 구현 | 선택 검토 | 기능 경계 |
| --- | --- | --- | --- |
| [[에픽] 공통 개발 기반·계약·통합 검증](https://github.com/team-bestfriend/danjjak-app/issues/1) | 5 | 0 | config·common·health / contracts·db·infra |
| [[에픽] 공통 화면·접근성·상태 복구](https://github.com/team-bestfriend/danjjak-app/issues/2) | 2 | 0 | frontend/router·components/common |
| [[에픽] 인증·설정·선택 동의](https://github.com/team-bestfriend/danjjak-app/issues/3) | 2 | 0 | auth·user |
| [[에픽] 사람·복수 계좌·모의 계좌 준비](https://github.com/team-bestfriend/danjjak-app/issues/4) | 2 | 0 | account |
| [[에픽] 단축번호·패턴 관리와 실행](https://github.com/team-bestfriend/danjjak-app/issues/5) | 3 | 0 | pattern |
| [[에픽] 음성·단계 안내·패턴 내 편집](https://github.com/team-bestfriend/danjjak-app/issues/6) | 4 | 1 | pattern 안내·tts / FE 음성 |
| [[에픽] 등록·직접 모의 송금](https://github.com/team-bestfriend/danjjak-app/issues/7) | 3 | 0 | transfer |
| [[에픽] 금융 조회·고객센터](https://github.com/team-bestfriend/danjjak-app/issues/8) | 2 | 0 | account 조회·support 고객센터 |
| [[에픽] 이상거래·보호자 대응](https://github.com/team-bestfriend/danjjak-app/issues/9) | 4 | 0 | transfer 판정·support 보호자 |
| [[에픽] 실행 기록·이용 분석·안내 개선](https://github.com/team-bestfriend/danjjak-app/issues/10) | 3 | 0 | pattern 기록·analysis |

## 하위 이슈와 선행 관계

| 이슈 | 상위 Epic | 선행 이슈 | 상태 |
| --- | --- | --- | --- |
| [#11 [구현] 공동 HTTP 계약과 데이터·종료 책임 확정](https://github.com/team-bestfriend/danjjak-app/issues/11) | [#1](https://github.com/team-bestfriend/danjjak-app/issues/1) | 없음 | [계약·물리 모델·정적 검증 완료](../../contracts/validation.md) |
| [#12 [구현] 개발·실행 환경 구성](https://github.com/team-bestfriend/danjjak-app/issues/12) | [#1](https://github.com/team-bestfriend/danjjak-app/issues/1) | [#11](https://github.com/team-bestfriend/danjjak-app/issues/11) | PR #44로 종료 · Spring 초기/문자열 health 반영, DB·health 계약 일치 후속 보완 |
| [#13 [구현] 모의 데이터와 MyBatis 저장 기반 구성](https://github.com/team-bestfriend/danjjak-app/issues/13) | [#1](https://github.com/team-bestfriend/danjjak-app/issues/1) | [#12](https://github.com/team-bestfriend/danjjak-app/issues/12) | 확정 구현 대기 |
| [#14 [구현] 네 탭과 읽기 쉬운 공통 조작 화면 구현](https://github.com/team-bestfriend/danjjak-app/issues/14) | [#2](https://github.com/team-bestfriend/danjjak-app/issues/2) | [#12](https://github.com/team-bestfriend/danjjak-app/issues/12) | 확정 구현 대기 |
| [#15 [구현] 공통 입력·팝업·오류와 새로고침 복구 구현](https://github.com/team-bestfriend/danjjak-app/issues/15) | [#2](https://github.com/team-bestfriend/danjjak-app/issues/2) | [#14](https://github.com/team-bestfriend/danjjak-app/issues/14), [#11](https://github.com/team-bestfriend/danjjak-app/issues/11) | 확정 구현 대기 |
| [#16 [구현] 고액·완료 송금 반복의 공통 서버 판정 구현](https://github.com/team-bestfriend/danjjak-app/issues/16) | [#9](https://github.com/team-bestfriend/danjjak-app/issues/9) | [#13](https://github.com/team-bestfriend/danjjak-app/issues/13) | 확정 구현 대기 |
| [#17 [구현] 카카오 로그인과 동일 사용자·세션 복구 구현](https://github.com/team-bestfriend/danjjak-app/issues/17) | [#3](https://github.com/team-bestfriend/danjjak-app/issues/3) | [#13](https://github.com/team-bestfriend/danjjak-app/issues/13), [#15](https://github.com/team-bestfriend/danjjak-app/issues/15) | 확정 구현 대기 |
| [#18 [구현] 독립 동의·접근성·온보딩·이용방법 구현](https://github.com/team-bestfriend/danjjak-app/issues/18) | [#3](https://github.com/team-bestfriend/danjjak-app/issues/3) | [#17](https://github.com/team-bestfriend/danjjak-app/issues/17) | 확정 구현 대기 |
| [#19 [구현] 모의 내 계좌 불러오기와 기본 계좌 선택 구현](https://github.com/team-bestfriend/danjjak-app/issues/19) | [#4](https://github.com/team-bestfriend/danjjak-app/issues/4) | [#18](https://github.com/team-bestfriend/danjjak-app/issues/18) | 확정 구현 대기 |
| [#20 [구현] 보호자 연락처 관리와 확인 후 전화 연결 구현](https://github.com/team-bestfriend/danjjak-app/issues/20) | [#9](https://github.com/team-bestfriend/danjjak-app/issues/9) | [#18](https://github.com/team-bestfriend/danjjak-app/issues/18), [#15](https://github.com/team-bestfriend/danjjak-app/issues/15) | 확정 구현 대기 |
| [#21 [구현] 사람별 복수 받는 계좌 등록·수정 구현](https://github.com/team-bestfriend/danjjak-app/issues/21) | [#4](https://github.com/team-bestfriend/danjjak-app/issues/4) | [#19](https://github.com/team-bestfriend/danjjak-app/issues/19) | 확정 구현 대기 |
| [#22 [구현] 템플릿 기반 패턴 등록·수정과 기본 업무 구현](https://github.com/team-bestfriend/danjjak-app/issues/22) | [#5](https://github.com/team-bestfriend/danjjak-app/issues/5) | [#21](https://github.com/team-bestfriend/danjjak-app/issues/21) | 확정 구현 대기 |
| [#23 [구현] 홈 페이지·번호 이동·교환·비활성화 구현](https://github.com/team-bestfriend/danjjak-app/issues/23) | [#5](https://github.com/team-bestfriend/danjjak-app/issues/5) | [#22](https://github.com/team-bestfriend/danjjak-app/issues/22), [#15](https://github.com/team-bestfriend/danjjak-app/issues/15) | 확정 구현 대기 |
| [#24 [구현] 동의 기반 실행·단계 방문·누적 행동 기록 구현](https://github.com/team-bestfriend/danjjak-app/issues/24) | [#10](https://github.com/team-bestfriend/danjjak-app/issues/10) | [#22](https://github.com/team-bestfriend/danjjak-app/issues/22), [#18](https://github.com/team-bestfriend/danjjak-app/issues/18) | 확정 구현 대기 |
| [#25 [구현] 패턴 안에서 시작·단계 공통 대본과 음성 설정 편집 구현](https://github.com/team-bestfriend/danjjak-app/issues/25) | [#6](https://github.com/team-bestfriend/danjjak-app/issues/6) | [#22](https://github.com/team-bestfriend/danjjak-app/issues/22), [#18](https://github.com/team-bestfriend/danjjak-app/issues/18), [#15](https://github.com/team-bestfriend/danjjak-app/issues/15) | 확정 구현 대기 |
| [#26 [구현] 실행 전 확인과 저장 순서의 단계 실행 구현](https://github.com/team-bestfriend/danjjak-app/issues/26) | [#5](https://github.com/team-bestfriend/danjjak-app/issues/5) | [#23](https://github.com/team-bestfriend/danjjak-app/issues/23), [#24](https://github.com/team-bestfriend/danjjak-app/issues/24) | 확정 구현 대기 |
| [#27 [구현] 현재 대본의 AI 음성과 접이식 안내·대체 재생 구현](https://github.com/team-bestfriend/danjjak-app/issues/27) | [#6](https://github.com/team-bestfriend/danjjak-app/issues/6) | [#25](https://github.com/team-bestfriend/danjjak-app/issues/25) | 확정 구현 대기 |
| [#28 [구현] 모의 송금 서버 검증과 금융·방문·실행 최종 확정 구현](https://github.com/team-bestfriend/danjjak-app/issues/28) | [#7](https://github.com/team-bestfriend/danjjak-app/issues/7) | [#21](https://github.com/team-bestfriend/danjjak-app/issues/21), [#24](https://github.com/team-bestfriend/danjjak-app/issues/24), [#16](https://github.com/team-bestfriend/danjjak-app/issues/16) | 확정 구현 대기 |
| [#29 [구현] 기간별 완료 횟수와 안내 검토 단계 분석 구현](https://github.com/team-bestfriend/danjjak-app/issues/29) | [#10](https://github.com/team-bestfriend/danjjak-app/issues/10) | [#24](https://github.com/team-bestfriend/danjjak-app/issues/24), [#23](https://github.com/team-bestfriend/danjjak-app/issues/23), [#15](https://github.com/team-bestfriend/danjjak-app/issues/15) | 확정 구현 대기 |
| [#30 [구현] 실제 가족 녹음과 대상별 저장·안전한 교체 구현](https://github.com/team-bestfriend/danjjak-app/issues/30) | [#6](https://github.com/team-bestfriend/danjjak-app/issues/6) | [#27](https://github.com/team-bestfriend/danjjak-app/issues/27) | 확정 구현 대기 |
| [#31 [구현] 번호·정해진 업무 음성 입력을 기존 확인창으로 연결](https://github.com/team-bestfriend/danjjak-app/issues/31) | [#6](https://github.com/team-bestfriend/danjjak-app/issues/6) | [#26](https://github.com/team-bestfriend/danjjak-app/issues/26), [#27](https://github.com/team-bestfriend/danjjak-app/issues/27) | 확정 구현 대기 |
| [#32 [구현] 등록 송금의 계좌 확인·금액·본인 확인 화면 구현](https://github.com/team-bestfriend/danjjak-app/issues/32) | [#7](https://github.com/team-bestfriend/danjjak-app/issues/7) | [#28](https://github.com/team-bestfriend/danjjak-app/issues/28), [#26](https://github.com/team-bestfriend/danjjak-app/issues/26), [#27](https://github.com/team-bestfriend/danjjak-app/issues/27) | 확정 구현 대기 |
| [#33 [구현] 내 계좌의 잔액·거래·분류별 금융 조회 구현](https://github.com/team-bestfriend/danjjak-app/issues/33) | [#8](https://github.com/team-bestfriend/danjjak-app/issues/8) | [#19](https://github.com/team-bestfriend/danjjak-app/issues/19), [#26](https://github.com/team-bestfriend/danjjak-app/issues/26), [#28](https://github.com/team-bestfriend/danjjak-app/issues/28) | 확정 구현 대기 |
| [#34 [구현] 고객센터 번호 조회와 전화 선택 완료 구현](https://github.com/team-bestfriend/danjjak-app/issues/34) | [#8](https://github.com/team-bestfriend/danjjak-app/issues/8) | [#26](https://github.com/team-bestfriend/danjjak-app/issues/26) | 확정 구현 대기 |
| [#35 [선택 검토] AI 음성 미리 생성의 지연·무효화·실패 비교](https://github.com/team-bestfriend/danjjak-app/issues/35) | [#6](https://github.com/team-bestfriend/danjjak-app/issues/6) | [#27](https://github.com/team-bestfriend/danjjak-app/issues/27) | 선택 검토 |
| [#36 [구현] 직접 입력 송금을 한 화면 한 입력으로 구현](https://github.com/team-bestfriend/danjjak-app/issues/36) | [#7](https://github.com/team-bestfriend/danjjak-app/issues/7) | [#32](https://github.com/team-bestfriend/danjjak-app/issues/32) | 확정 구현 대기 |
| [#37 [구현] 이상거래 경고·재확인·계속·취소 흐름 구현](https://github.com/team-bestfriend/danjjak-app/issues/37) | [#9](https://github.com/team-bestfriend/danjjak-app/issues/9) | [#32](https://github.com/team-bestfriend/danjjak-app/issues/32) | 확정 구현 대기 |
| [#38 [구현] 안내 문구 제안 비교·명시적 적용·같은 단계 재녹음 연결](https://github.com/team-bestfriend/danjjak-app/issues/38) | [#10](https://github.com/team-bestfriend/danjjak-app/issues/10) | [#29](https://github.com/team-bestfriend/danjjak-app/issues/29), [#30](https://github.com/team-bestfriend/danjjak-app/issues/30) | 확정 구현 대기 |
| [#39 [구현] 높은 주의의 카카오 본인 전송 시연 구현](https://github.com/team-bestfriend/danjjak-app/issues/39) | [#9](https://github.com/team-bestfriend/danjjak-app/issues/9) | [#37](https://github.com/team-bestfriend/danjjak-app/issues/37), [#18](https://github.com/team-bestfriend/danjjak-app/issues/18) | 확정 구현 대기 |
| [#40 [구현] 민감 정보 배제와 외부 실패의 업무 격리 검증·보완](https://github.com/team-bestfriend/danjjak-app/issues/40) | [#1](https://github.com/team-bestfriend/danjjak-app/issues/1) | [#36](https://github.com/team-bestfriend/danjjak-app/issues/36), [#39](https://github.com/team-bestfriend/danjjak-app/issues/39), [#31](https://github.com/team-bestfriend/danjjak-app/issues/31), [#30](https://github.com/team-bestfriend/danjjak-app/issues/30), [#38](https://github.com/team-bestfriend/danjjak-app/issues/38), [#33](https://github.com/team-bestfriend/danjjak-app/issues/33) | 확정 구현 대기 |
| [#41 [구현] 전체 시나리오·실기기·반복 시연 검증](https://github.com/team-bestfriend/danjjak-app/issues/41) | [#1](https://github.com/team-bestfriend/danjjak-app/issues/1) | [#40](https://github.com/team-bestfriend/danjjak-app/issues/40), [#20](https://github.com/team-bestfriend/danjjak-app/issues/20), [#34](https://github.com/team-bestfriend/danjjak-app/issues/34) | 확정 구현 대기 |

- 번호는 실제 GitHub 생성 결과입니다. 각 이슈 본문의 선행 이슈와 네이티브 관계를 함께 확인했습니다.
- 실행 기록은 분석 Epic에서 추적하되 운영 파일 위치는 기존 `pattern` 계층을 유지합니다.
- 공통 기반의 최종 통합 이슈가 SC-001–018 전체 결과를 모으고, 각 기능 이슈는 담당 결과의 검증을 소유합니다.
- 이슈 생성·열림 상태는 기능 구현 착수나 대회 반입 승인 근거가 아닙니다.
