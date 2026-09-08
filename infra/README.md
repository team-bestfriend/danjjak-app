# 로컬 MySQL·Flyway 실행

Docker Compose로 MySQL 8.4와 Flyway를 실행합니다. 백엔드와 프론트엔드 자체는
컨테이너에 포함하지 않습니다.

## 준비와 실행

Docker Desktop을 실행한 뒤 로컬 환경 파일을 준비합니다.

```powershell
cd C:\danjjak-app\infra
Copy-Item .env.example .env
docker compose up -d
docker compose logs flyway --tail 50
docker compose ps -a
```

- `mysql`이 `healthy`여야 합니다.
- `flyway`가 `Exited (0)`이고 로그에 전체 적용 성공이 표시되어야 합니다.
- `.env`는 Git에서 제외되며 `.env.example`만 공유합니다.

Flyway를 다시 실행해 중복 적용이 없는지 확인합니다.

```powershell
docker compose run --rm flyway migrate
```

로그에 `Schema is up to date`가 표시되면 정상입니다.

## 데이터 유지와 초기화

MySQL 자료는 `mysql-data` 볼륨에 저장되므로 컨테이너를 다시 만들어도 유지됩니다.

```powershell
docker compose down
docker compose up -d
```

깨끗한 DB 검증이 필요할 때만 볼륨을 삭제합니다. 이 명령은 기존 로컬 DB 자료를 모두 지웁니다.

```powershell
docker compose down -v
docker compose up -d
docker compose logs flyway --tail 50
```

## 백엔드 연결

기본 접속값은 `backend/src/main/resources/application.properties`와 일치합니다.

```text
jdbc:mysql://localhost:3306/danjjak?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
사용자: danjjak
비밀번호: danjjak_local
```

포트를 바꾼 경우 백엔드 실행 환경의 `DANJJAK_DB_URL`도 같은 포트로 맞춥니다.

## 문제 확인

```powershell
docker compose logs mysql --tail 100
docker compose logs flyway --tail 100
docker compose config --quiet
```

적용된 마이그레이션의 체크섬이 달라졌다면 DB를 먼저 삭제하지 말고 원본 파일을 복구합니다.
자세한 버전 규칙은 [`db/README.md`](../db/README.md)를 확인합니다.
