# 데이터베이스 스키마

`db/migration/`은 단짝의 MySQL 물리 모델과 모의 초기 자료의 기준입니다.
원본 `team-bestfriend/danjjak@a296758`의 V1~V7 이력을 그대로 보존하고,
현재 명세와의 차이는 후속 버전에서 보완합니다.

## 마이그레이션 구성

| 버전 | 내용 |
| --- | --- |
| V1 | 사용자·계좌·패턴·실행·거래·이상거래 스키마 |
| V2 | 모의 사용자·보호자·계좌·거래 자료 |
| V3 | 동의 완료와 이용 기록·보호자 공유 선택값 |
| V4~V7 | 7종 템플릿, 기본 패턴 8개, 시작·단계 안내와 가족 음성 상태 |
| V8 | 본인 계좌 후보와 불러온 계좌를 구분하는 `imported_at` |
| V9 | 불러오기 초기 상태와 사람별 복수 받는 계좌 자료 |

스키마와 모의 자료는 별도 버전에 둡니다. V8은 구조만 바꾸고 V9가 자료를 보완합니다.

## 작성 규칙

- 파일 이름은 `V{정수}__{영문_소문자_설명}.sql` 형식을 사용합니다.
- 공유되거나 적용된 마이그레이션은 수정·삭제·이름 변경하지 않습니다.
- 변경이 필요하면 다음 번호의 새 마이그레이션을 추가합니다.
- 원문 PIN, OAuth 토큰, 음성 바이너리, 실금융 자격정보를 저장하지 않습니다.
- 모의 PIN은 단방향 해시만 저장하고 가족 음성은 파일 경로·형식·불일치 상태만 저장합니다.

## 실행과 확인

MySQL과 Flyway 실행 방법은 [`infra/README.md`](../infra/README.md)를 따릅니다.
Flyway 로그에서 전체 적용 성공 또는 `Schema is up to date`를 반드시 확인합니다.

핵심 시드는 다음 쿼리로 확인할 수 있습니다.

```sql
SELECT COUNT(*) FROM financial_patterns WHERE is_active = TRUE;
SELECT pattern_type, COUNT(*) FROM financial_patterns GROUP BY pattern_type;
SELECT COUNT(*) FROM financial_patterns WHERE description IS NULL OR description = '';
SELECT COUNT(*) FROM pattern_steps WHERE instruction_text IS NULL OR instruction_text = '';

SELECT imported_at IS NULL AS is_candidate, COUNT(*)
FROM bank_accounts
WHERE registered_person_id IS NULL
GROUP BY imported_at IS NULL;

SELECT rp.name, COUNT(*) AS account_count
FROM registered_persons rp
JOIN bank_accounts ba ON ba.registered_person_id = rp.registered_person_id
GROUP BY rp.registered_person_id, rp.name
HAVING COUNT(*) > 1;
```
