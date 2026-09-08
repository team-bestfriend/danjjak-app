# 설계 검증

아래는 설계 준비 시점의 명세·설계·개발 이슈 검증 기록입니다. 이후 추가한 빌드·공동 계약의 현황은 [개발 준비 현황](development-status.md)과 [#11 계약 검증](../contracts/validation.md)을 따릅니다. 앱 연동·SC 시나리오 통과를 뜻하지 않습니다.

| 검사 | 결과·방법 |
| --- | --- |
| Java 경계 | 81개 파일에서 Javadoc을 제거한 뒤 `package`와 빈 `public class/interface` 선언만 있는지 검사 |
| Java 설명 | 모든 선언의 한국어 역할·책임·협력·요구사항 ID·실재 명세 링크 확인 |
| 금지 구성 | 필드·생성자·메서드·import·상속/구현 의존·어노테이션·실행 설정·SQL·시드·구현 테스트 없음 |
| 링크·라우팅 | 로컬 문서·Java 링크와 앵커 확인, Markdown 47개 모두 AGENTS에서 도달. CLAUDE.md는 AGENTS.md만 참조 |
| README | GitHub Markdown 렌더링에서 제목 22개·표 2개·배지 5개 확인 |
| 스킬 | 5개 SKILL.md의 frontmatter·이름·설명과 참고 링크 확인 |
| 요구사항 | FR 61 / UX 14 / NFR 12 / SC 18 / CH 34 / D 5 = 144개 정의와 실제 이슈 연결, 누락·중복 행 없음 |
| 선택·설계안 | FR-061·CH-13·D-03 선택 상태와 D 설계안 유지, 확정 작업의 선택 이슈 의존 없음 |
| 실제 이슈 | Epic 10개·구현 30개·선택 검토 1개, 본문·라벨·열림 상태를 API로 조회 |
| 네이티브 관계 | 하위 관계 31개와 선행 작업 관계 56개 확인, 의존 순환 없음 |
| 이슈 링크 | `main`의 파일 경로와 요구사항 링크 확인 |
| 파일 허용 목록 | Markdown 47개·Java 81개·.gitignore·.editorconfig, 총 130개 |
| 민감값 | UTF-8·토큰/개인키 패턴·허용 파일 검사에서 검출 없음 |

- 앱 빌드·Java 컴파일·DB 실행·외부 서비스 호출·SC-001–018은 수행하지 않았습니다.
- 실제 제공사와 실기기 결과는 해당 구현 이슈에 환경·실행 결과·증거를 남깁니다.

[개발 준비 현황](development-status.md) · [이슈 지도](issues/issue-map.md) · [요구사항 추적표](issues/requirement-map.md)
