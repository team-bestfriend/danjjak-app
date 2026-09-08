# HTTP 계약

[OpenAPI 1.0.0](openapi.yaml)이 #11에서 정한 FE·BE 공통 경로·필드·상태 코드의 기준입니다. Spring Controller/DTO와 FE API 클라이언트를 이 파일에 맞춰 구현합니다. 현재 계약 검증 완료이며 서버 API 구현·실제 연동 완료를 뜻하지 않습니다.

| 자료 | 용도 |
| --- | --- |
| [OpenAPI](openapi.yaml) | 46개 경로·54개 동작·100개 스키마 |
| [결정과 책임](decisions.md) | 인증·식별자·부분 저장·송금/기록 확정·오류 코드 |
| [물리 데이터 모델](../docs/design/data-model.md) | #13의 컬럼·키·관계·소유 mapper 구현 기준 |
| [FE 이관 차이](frontend-handoff.md) | 기존 클라이언트와의 차이 및 연결 순서 |
| [요구사항 연결](coverage.md) | 확정 FR의 서버/FE 경계, 관련 SC 검증 계획 |
| [검증 기록](validation.md) | 실제 수행한 계약 검사와 미검증 실행 범위 |
| [가상 예제](examples/) | 세션·송금 제출·경고 응답; 실제 사용자/금융/자격정보 아님 |

## 검증

Node `20.19.x` 또는 `22.12+`를 사용합니다. 검증 도구는 `package-lock.json`에 고정했습니다.

```powershell
cd contracts
npm ci --ignore-scripts
npm run check
```

Redocly CLI 2.51.2의 recommended-strict와 JSON Schema 경계값 검사를 실행합니다. OAuth의 정상 302 이동은 일반 2xx 권고의 예외이며 테스트가 해당 두 동작만 예외로 확인합니다. API에 별도 라이선스를 선언하지 않아 info-license 규칙만 비활성화했습니다. 클라이언트/서버 코드는 생성하지 않습니다.

API 변경은 OpenAPI·해당 결정·FE 차이·검증 예제를 함께 갱신합니다. 실제 제공사·SC 시나리오는 후속 구현 이슈에서 검증합니다. D-03/FR-061의 선택적 사전 생성은 필수 API에 포함하지 않습니다.

[개발 이슈](../docs/issues/issue-map.md) · [개발 범위](../docs/specs/requirements/delivery-constraints.md)
