# 백엔드 설계

[명세 목차](../specs/requirements.md) · [Java 설계 파일](backend-inventory.md) · [데이터 관계](data-model.md)

## 패키지와 계층

- 기본 경로: `backend/src/main/java/com/bestfriend/danjjak`, 설계 선언 81개.
- 기능 패키지 안에서 `controller → service → mapper`로 책임을 나눕니다. `dto`는 외부 입출력, MyBatis `model`은 업무 데이터·조회 투영·저장 명령을 표현합니다.
- JDK 17·Spring Framework 5.3·MyBatis·Gradle WAR·Tomcat 9를 사용합니다. Spring Boot·JPA·별도 `domain` 패키지는 도입하지 않습니다.
- 현재 Java는 한국어 Javadoc과 빈 클래스·인터페이스 선언입니다. 필드·메서드·생성자·어노테이션·실행 설정은 담당 구현 이슈에서 작성합니다.

| 기능 패키지 | 계층 | 책임·협력 |
| --- | --- | --- |
| `auth` | controller/service/dto | 카카오 인증·세션, `user` 연결 |
| `user` | controller/service/mapper/dto/model | 사용자·독립 선택 동의·접근성 |
| `account` | controller/service/mapper/dto/model | 내 계좌·등록 사람·복수 받는 계좌·금융 조회 |
| `pattern` | controller/service/mapper/dto/model | 번호·템플릿·단계·실행/방문·안내·가족 파일 |
| `tts` | controller/service/dto | AI 합성 요청과 제공사 경계 |
| `transfer` | controller/service/mapper/dto/model | 모의 송금·PIN 비교·FDS 정책·판정·최종 확정 |
| `support` | controller/service/mapper/dto/model | 보호자 연락처·고객센터·카톡 본인 시연 |
| `analysis` | controller/service/mapper/dto/model | 동의 기반 기간 집계·안내 제안·명시적 적용 |
| `common` | error/session | 공통 오류 표현·세션 사용자 확인 |
| `config`, `health` | 구성/상태 확인 | 본선 환경 연결 예정 위치 |

## 주요 설계 책임

| 주제 | 책임·협력 | 근거 |
| --- | --- | --- |
| 사람·받는 계좌 | `RegisteredPersonRecord`는 사람 식별, `RegisteredPersonAccountRecord`는 결합 조회, `RecipientAccountCommand`는 독립 계좌 저장 | [FR-005–006, SC-017](../specs/requirements/people-accounts.md) |
| 내 계좌 준비 | `account`가 모의 후보 선택·중복 없는 추가를 조정하고 `OwnedAccountImportCommand`로 저장 의도 표현 | [FR-058, SC-016](../specs/requirements/people-accounts.md) |
| 최종 확정 | `TransferService`가 금융 결과·마지막 방문·실행을 함께 확정. 일반 조회·전화 종료와 실행/방문 기록은 `PatternController/Service/Mapper` 담당 | [FR-031](../specs/requirements/mock-transfer.md), [FR-044–047](../specs/requirements/usage-analysis.md), [SC-015](../specs/requirements/validation-scenarios.md) |
| 안내·파일 | `GuidanceService`가 대상별 저장·불일치·부분 재시도, `VoiceFileStore`가 파일 교체 담당 | [FR-019–026, 054–056](../specs/requirements/guidance-voice.md) |
| 이용 분석 | `analysis`가 기간·비활성 기록을 보존하며 집계하고, `GuidanceService`를 통해 문구 비교 후 변경 저장 | [FR-048–052](../specs/requirements/usage-analysis.md) |
| 선택 평가 | FR-061/D-03은 채택 전 지연·실패·무효화를 비교하는 선택 이슈 | [D-03](../specs/requirements/design-decisions.md) |

- HTTP 필드, 물리 컬럼, 라이브러리 호출, 구현 테스트는 담당 이슈에서 결정·작성합니다.
