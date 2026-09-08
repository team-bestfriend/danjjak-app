# FE 이관과 계약 적용

기준은 [OpenAPI 1.0.0](openapi.yaml)입니다. #11 착수 시 기존 `danjjak/frontend/src/api`의 8개 클라이언트와 `stores/appStore.js` 소비 코드를 대조했습니다. 작업 중 FE 이관 PR #43과 Spring 초기 설정 PR #44가 main(`6ae245e`)에 병합되어 이관된 6개 API 파일·라우터·콜백·Vite 프록시와 health 구현도 추가 확인했습니다. **이 문서는 계약 적용 차이 목록이며 실제 FE 수정·연동 성공 증거가 아닙니다.** 후속 적용은 [FE 이관 #42](https://github.com/team-bestfriend/danjjak-app/issues/42)와 해당 기능 이슈에서 이어갑니다.

현재 이관본에는 `guidanceApi.js`와 `instructionSuggestionApi.js`가 아직 없습니다. 아래 해당 행은 이전 레포의 추가 구현을 참고한 이관 기준입니다.

## 유지하는 접점

- `/api/auth/kakao/start`, `/api/auth/session`, `/api/auth/logout`, `/api/users/me` 및 설정/동의 경로.
- 계좌·사람·패턴·안내·송금·판정 결정·고객센터·TTS·분석의 기존 `/api` 경로 체계.
- `credentials: 'include'`, 성공 객체/배열 직접 반환, 오류 `code/message`, 204 빈 응답, TTS 성공 바이너리.
- 기존 `TTS/FAMILY`, `SLOW/NORMAL/FAST`, 7종 `patternType` 명칭. 제품 화면에서는 TTS 대신 “AI 음성”으로 표시합니다.

## 파일별 적용할 차이

| 기존 위치 | 현재 관찰 | 적용할 계약 |
| --- | --- | --- |
| `authApi.js`, 로그인 화면 | 새 이관본에 `/api/auth/dev-session` 호출 추가 | 현재 명세의 카카오 성공과 모의 사용자 연결만 계약에 포함. dev-session은 계약 API가 아니며 카카오 취소/실패를 로그인 성공으로 대체하지 않음 |
| `AuthCallbackView.vue`, 라우터 | `/auth/callback?status=success/cancelled` 처리 | 이 경로와 status 값을 유지. 실패는 failed + 안전한 ErrorCode로 매핑, 토큰/카카오 인증 코드는 FE에 전달하지 않음 |
| `vite.config.js` | `/api`를 localhost:8080의 `/danjjak/api`로 rewrite | 이 개발 프록시 사용 시 VITE_API_BASE_URL은 빈 값 유지. Tomcat 직접 호출/배포 환경에서는 context를 한 번만 붙임 |
| `httpClient.js`, `ttsApi.js` | 세션 쿠키만 전달 | 세션 응답 `csrfToken` 보관, 상태 변경/합성에 `X-CSRF-Token` 추가. 만료 시 토큰과 진행 PIN 제거. 오류 `fieldErrors/retryable/requestId` 사용 가능 |
| `authApi.js`, 사용자 소비 코드 | 사용자/동의 구조에 의존 | `CurrentUser`, `Consents`, `Settings` 스키마로 통일. 두 동의 false와 `completed=false`를 구별 |
| `financeApi.js`, `mapOwnedAccount` | 전체 `accountNumber`를 FE에서 마스킹 | 서버 `maskedAccountNumber` 그대로 표시. 전체 번호는 수정 전용 API에서만 조회 |
| `mapRegisteredPerson`, `accountsByPerson` | `person.account` 한 개 가정 | `person.accounts[]`와 `accountCount`, 별도 `recipientAccountId`. 같은 사람 두 번째 계좌의 추가/수정 API 연결 |
| `financeApi.js` | 내 계좌 불러오기/기본 변경 API 없음 | `listImportCandidates`, `importOwnedAccounts`, `setDefaultAccount`, `listBanks` 추가 |
| `patternApi.js`, `toUiPattern` | `linked.accountId`, number형 ID 사용 가능 | `linkedAccount.recipientAccountId`, 모든 DB ID 문자열. 기존 미지원 AUTO_TRANSFER/CARD/DEPOSIT 종류는 7종 계약에 포함하지 않음 |
| `patternApi.js` | 번호 목록 items만 전송 | order GET의 revision + 전체 목록 + `swapConfirmed`. 비활성화도 expectedRevision 전달 |
| 이전 레포 `guidanceApi.js`, `withGuidance` | `start`/`stepCode` 문자열로 대상 결합; 현재 main에는 미이관 | START는 kind, STEP은 실제 `stepId`로 매핑. 저장/업로드 경로는 서버 `targetId`. 단계 순서나 이름으로 대상 식별 금지 |
| `saveGuidanceDraft` | 문구 저장 후 파일 저장 | 저장 응답 revision + `uploadId` + `recordedText`를 파일과 함께 전달. `patternId`와 대상별 저장 성공을 유지하고 실패 부분만 재시도 |
| `patternApi.startExecution` | 시작 때 기본 출금 계좌 전송; 이전 레포 추가본은 별도 선조회 안내를 결합 | 시작은 requestId만 전송. 응답 최신 패턴의 단계 ID로 안내 조회. 금융 확정 요청의 실제 출금 ID가 권위. 기록 거절은 execution=null |
| `startVisit/updateVisit` | 단계 진입/누적값 형태 변경 필요 | `visitId` UUID·관측 시각·이전 방문 종료를 제출. 작은 합계 재시도는 중복 합산하지 않음 |
| `finishExecution` | 임의 `{status}`로 종료 | 일반 종료는 CANCELLED/FAILED + 마지막 방문/시각. 조회 완료와 전화 선택은 전용 API. 송금 확정 후 별도 종료 호출 제거 |
| `createTransfer`, `validateTransferResponse` | attemptId 없음, 정상만 특정 형태 가정, 거래 ID를 숫자로 검사 | `TransferRequest`의 attemptId/context/받는 계좌 분기/숫자·한글 금액. `COMPLETED/REQUIRES_REVIEW/CANCELLED` 분기 스키마 사용. 경고 대기에 balanceAfter/transactionId가 없음을 허용 |
| `resolveAnomaly`, `validateResolutionResponse` | 별도 action 중심 응답 가정 | 송금과 동일 `TransferResult`. 중복/반대 결정은 저장 결과 우선. 조회/응답 검증 실패에 자동 재송금 금지 |
| `financeApi.js` | 결과 재조회/재확인 없음 | `getTransferResult`, `getAnomaly`, `recheckAnomaly` 추가. 경고 선택·알림에 같은 anomalyEventId 유지 |
| `notifyGuardian` | 본문 없는 요청 | `confirmedSelfDemo=true` 필수. ACTUAL/MOCK_NO_CREDENTIALS/MOCK_AFTER_FAILURE 구분. 동의 없음은 실제·모의 모두 금지 |
| `usageAnalysisApi.js`, 분석 화면 | 기존 보고서 필드에 의존 | `UsageAnalysis`의 state/period/patterns/candidates, 문자열 ID·nullable 시간·완료0회 행 지원 |
| 이전 레포 `instructionSuggestionApi.apply` | expectedText만 전송; 현재 main에는 미이관 | 비교 응답의 기간·현재/제안 문구·expectedRevision 모두 전달. 반환 targetId로 같은 단계 녹음 연결 |

## 현재 BE 실행 설정과의 차이

PR #44의 `HealthController`는 `GET /api/health`에서 문자열 `OK`를 반환하며 DB를 조회하지 않습니다. #11의 health 계약은 실제 DB 준비 시 `200 {"status":"UP","database":"UP"}`, 설정 누락/실패는 `503 ApiError`입니다. 현재 health 구현을 DB 준비 완료 증거로 사용하지 않습니다. DB/MyBatis 기반 #13과 연결할 때 HealthService/Mapper 및 오류 응답을 이 계약으로 맞춰야 합니다. #11에서는 팀원의 Spring 초기 설정을 변경하지 않았습니다.

## 첫 연결 순서

1. 세션·CSRF·공통 오류와 상태 확인 API 연결(#12/#15/#17).
2. 동의 false/false 저장 후에도 설정/모의 금융을 사용할 수 있는지 확인(#18).
3. 내 계좌 불러오기→사람 첫/두 번째 계좌→패턴 연결(#19/#21/#22).
4. 동의 유무별 시작·방문·조회 종료, 정상/경고 송금 및 응답 유실 재조회(#24/#26/#28).
5. 대상별 문구/녹음 부분 저장, 분석 비교·적용(#25/#30/#38).

요구사항을 실제 화면에 적용하면서 발견한 차이는 #42에 기록합니다. 계약 변경이 필요하면 OpenAPI·이 문서·계약 예제 검증을 같은 변경으로 갱신합니다. 초기 FE API 호출 목록만으로 새 서버의 기능이 구현됐다고 판단하지 않습니다.
