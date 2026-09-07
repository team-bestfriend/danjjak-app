# 백엔드 Java 설계 파일

기능별 패키지의 설계 선언 81개입니다. 각 파일의 한국어 Javadoc에 책임·협력 대상·요구사항 ID·명세 링크를 적었습니다.

[패키지와 계층](backend-design.md) · [데이터 관계](data-model.md)

| Java 파일 | 선언 | 역할 |
| --- | --- | --- |
| [account/controller/AccountController.java](../../backend/src/main/java/com/bestfriend/danjjak/account/controller/AccountController.java) | `class` | 내 계좌·사람·받는 계좌·조회 요청의 입출력 경계 |
| [account/dto/AccountDtos.java](../../backend/src/main/java/com/bestfriend/danjjak/account/dto/AccountDtos.java) | `class` | 사람별 복수 계좌와 내 계좌·조회 결과의 외부 표현 설계 |
| [account/mapper/AccountMapper.java](../../backend/src/main/java/com/bestfriend/danjjak/account/mapper/AccountMapper.java) | `interface` | 사용자 소유 계좌·사람·개별 받는 계좌·거래의 저장 및 조회 경계 |
| [account/model/AccountRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/account/model/AccountRecord.java) | `class` | 조회·출금에 쓰는 모의 본인 계좌의 업무 모델 |
| [account/model/OwnedAccountImportCommand.java](../../backend/src/main/java/com/bestfriend/danjjak/account/model/OwnedAccountImportCommand.java) | `class` | 선택한 모의 본인 계좌 후보를 기존 자료 보존과 함께 추가하는 명령 |
| [account/model/RecipientAccountCommand.java](../../backend/src/main/java/com/bestfriend/danjjak/account/model/RecipientAccountCommand.java) | `class` | 기존 사람의 특정 계좌 추가·수정 의도를 독립적으로 전달하는 저장 명령 |
| [account/model/RegisteredPersonAccountRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/account/model/RegisteredPersonAccountRecord.java) | `class` | 사람과 받는 계좌를 결합해 복수 계좌 목록을 구성하는 조회 모델 |
| [account/model/RegisteredPersonCommand.java](../../backend/src/main/java/com/bestfriend/danjjak/account/model/RegisteredPersonCommand.java) | `class` | 사람 추가·수정과 최초 계좌 등록 의도를 전달하는 저장 명령 |
| [account/model/RegisteredPersonRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/account/model/RegisteredPersonRecord.java) | `class` | 계좌와 분리된 사람 식별·이름·관계를 표현하는 업무 모델 |
| [account/model/TransactionRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/account/model/TransactionRecord.java) | `class` | 저장된 모의 거래와 당시 잔액·받는 정보를 조회하는 모델 |
| [account/service/AccountService.java](../../backend/src/main/java/com/bestfriend/danjjak/account/service/AccountService.java) | `class` | 소유권·계좌 형식·복수 계좌·모의 불러오기와 금융 조회를 조정 |
| [analysis/controller/InstructionSuggestionController.java](../../backend/src/main/java/com/bestfriend/danjjak/analysis/controller/InstructionSuggestionController.java) | `class` | 안내 제안 비교와 명시적 적용의 입출력 경계 |
| [analysis/controller/UsageAnalysisController.java](../../backend/src/main/java/com/bestfriend/danjjak/analysis/controller/UsageAnalysisController.java) | `class` | 사용자 기간별 이용 분석 요청과 상태의 입출력 경계 |
| [analysis/dto/InstructionSuggestionDtos.java](../../backend/src/main/java/com/bestfriend/danjjak/analysis/dto/InstructionSuggestionDtos.java) | `class` | 현재·제안 문구 비교와 재비교 결과의 외부 표현 설계 |
| [analysis/dto/UsageAnalysisDtos.java](../../backend/src/main/java/com/bestfriend/danjjak/analysis/dto/UsageAnalysisDtos.java) | `class` | 동의·자료 유무·기간·업무 횟수·검토 단계의 외부 표현 설계 |
| [analysis/mapper/UsageAnalysisMapper.java](../../backend/src/main/java/com/bestfriend/danjjak/analysis/mapper/UsageAnalysisMapper.java) | `interface` | 종료 실행과 방문을 기간별 집계하고 비활성 기록을 보존하는 조회 경계 |
| [analysis/model/PatternUsageRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/analysis/model/PatternUsageRecord.java) | `class` | 패턴별 완료 횟수와 표시 순서·활성 상태를 나타내는 집계 모델 |
| [analysis/model/StepAnalysisRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/analysis/model/StepAnalysisRecord.java) | `class` | 같은 단계의 행동 합계와 실측 시간으로 검토 후보를 나타내는 모델 |
| [analysis/service/InstructionSuggestionService.java](../../backend/src/main/java/com/bestfriend/danjjak/analysis/service/InstructionSuggestionService.java) | `class` | 정해진 문구 제안·원문 비교·같은 안내 대상으로의 저장을 조정 |
| [analysis/service/UsageAnalysisService.java](../../backend/src/main/java/com/bestfriend/danjjak/analysis/service/UsageAnalysisService.java) | `class` | 동의와 한국 날짜 기간을 확인하고 횟수·후보를 결정적으로 정렬 |
| [auth/controller/AuthController.java](../../backend/src/main/java/com/bestfriend/danjjak/auth/controller/AuthController.java) | `class` | 카카오 로그인·콜백·세션 확인·로그아웃의 입출력 경계 |
| [auth/dto/AuthDtos.java](../../backend/src/main/java/com/bestfriend/danjjak/auth/dto/AuthDtos.java) | `class` | 인증 성공·취소·실패·세션 상태의 외부 표현 설계 |
| [auth/service/KakaoOAuthClient.java](../../backend/src/main/java/com/bestfriend/danjjak/auth/service/KakaoOAuthClient.java) | `class` | 카카오 인증 제공사 요청과 실패를 격리하는 연동 경계 |
| [auth/service/KakaoOAuthService.java](../../backend/src/main/java/com/bestfriend/danjjak/auth/service/KakaoOAuthService.java) | `class` | 검증된 카카오 사용자와 모의 사용자 연결 및 세션 수명 조정 |
| [common/error/ApiError.java](../../backend/src/main/java/com/bestfriend/danjjak/common/error/ApiError.java) | `class` | 공통 오류와 사용자에게 필요한 다음 행동의 외부 표현 설계 |
| [common/error/ApiException.java](../../backend/src/main/java/com/bestfriend/danjjak/common/error/ApiException.java) | `class` | 업무 실패 원인을 공통 응답 경계로 전달하는 오류 역할 |
| [common/error/GlobalExceptionHandler.java](../../backend/src/main/java/com/bestfriend/danjjak/common/error/GlobalExceptionHandler.java) | `class` | 업무·입력 오류를 쉬운 공통 응답으로 정리하고 민감값 노출을 방지 |
| [common/session/DemoSessionUserResolver.java](../../backend/src/main/java/com/bestfriend/danjjak/common/session/DemoSessionUserResolver.java) | `class` | 세션의 인증 사용자 확인과 만료 처리를 담당하는 공통 경계 |
| [config/RootConfig.java](../../backend/src/main/java/com/bestfriend/danjjak/config/RootConfig.java) | `class` | 본선의 데이터 소스·트랜잭션·MyBatis·서비스 연결을 배치할 구성 역할 |
| [config/WebAppInitializer.java](../../backend/src/main/java/com/bestfriend/danjjak/config/WebAppInitializer.java) | `class` | 외부 Tomcat의 웹 애플리케이션 초기화 연결을 배치할 역할 |
| [config/WebConfig.java](../../backend/src/main/java/com/bestfriend/danjjak/config/WebConfig.java) | `class` | 본선의 MVC 입출력·리소스·오류 경계를 배치할 구성 역할 |
| [health/controller/HealthController.java](../../backend/src/main/java/com/bestfriend/danjjak/health/controller/HealthController.java) | `class` | 본선 환경과 DB 준비 상태 확인의 입출력 경계 |
| [health/mapper/HealthMapper.java](../../backend/src/main/java/com/bestfriend/danjjak/health/mapper/HealthMapper.java) | `interface` | 본선 DB 연결 가능 상태를 확인할 조회 경계 |
| [health/service/HealthService.java](../../backend/src/main/java/com/bestfriend/danjjak/health/service/HealthService.java) | `class` | 애플리케이션과 DB의 실제 준비 상태 확인을 조정 |
| [pattern/controller/GuidanceController.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/controller/GuidanceController.java) | `class` | 시작·단계 안내의 조회·개별 저장·녹음 교체 입출력 경계 |
| [pattern/controller/PatternController.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/controller/PatternController.java) | `class` | 패턴 관리·확인 후 시작·동의한 실행과 방문 기록의 입출력 경계 |
| [pattern/dto/GuidanceDtos.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/dto/GuidanceDtos.java) | `class` | 안내 대상·공통 문구·음성 방식·녹음 불일치의 외부 표현 설계 |
| [pattern/dto/PatternDtos.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/dto/PatternDtos.java) | `class` | 템플릿·패턴·순서·실행·방문 결과의 외부 표현 설계 |
| [pattern/mapper/GuidanceMapper.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/mapper/GuidanceMapper.java) | `interface` | 대상별 문구·방식·가족 파일 참조·불일치의 저장 경계 |
| [pattern/mapper/PatternMapper.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/mapper/PatternMapper.java) | `interface` | 패턴·순서·단계·동의한 실행과 방문의 기능 단위 저장 경계 |
| [pattern/model/ExecutionCommand.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/model/ExecutionCommand.java) | `class` | 패턴 시작과 실제 선택 내 계좌의 연결 의도를 전달하는 명령 |
| [pattern/model/GuidanceRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/model/GuidanceRecord.java) | `class` | 시작 또는 특정 단계의 저장 문구·방식·파일 불일치를 나타내는 모델 |
| [pattern/model/PatternCommand.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/model/PatternCommand.java) | `class` | 번호·업무·특정 받는 계좌의 등록 및 수정 의도를 전달하는 명령 |
| [pattern/model/PatternRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/model/PatternRecord.java) | `class` | 활성 번호·업무·연결 계좌와 과거 참조를 표현하는 패턴 업무 모델 |
| [pattern/model/PatternStepRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/model/PatternStepRecord.java) | `class` | 단계 식별과 저장 순서·기본 안내를 구분하는 단계 업무 모델 |
| [pattern/model/StepCommand.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/model/StepCommand.java) | `class` | 템플릿에 따른 단계 생성·문구 저장 의도를 전달하는 명령 |
| [pattern/model/StepVisitRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/model/StepVisitRecord.java) | `class` | 재진입별 방문과 누적 행동·실측 시작 및 종료를 나타내는 모델 |
| [pattern/service/GuidanceService.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/service/GuidanceService.java) | `class` | 공통 대본·대상별 명시 저장·기본값·파일 교체와 불일치를 조정 |
| [pattern/service/PatternCatalog.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/service/PatternCatalog.java) | `class` | 일곱 업무 템플릿의 단계·기본 문구 의도를 제공하는 도메인 역할 |
| [pattern/service/PatternService.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/service/PatternService.java) | `class` | 패턴 관리·단계 순서·동의한 실행과 방문 수명을 조정; 송금 최종 확정은 송금 서비스 소유 |
| [pattern/service/VoiceFileStore.java](../../backend/src/main/java/com/bestfriend/danjjak/pattern/service/VoiceFileStore.java) | `class` | 실제 녹음의 형식·소유 범위·참조와 안전한 교체 순서를 담당 |
| [support/controller/SupportController.java](../../backend/src/main/java/com/bestfriend/danjjak/support/controller/SupportController.java) | `class` | 보호자 연락처·고객센터 번호·카톡 시연의 입출력 경계 |
| [support/dto/SupportDtos.java](../../backend/src/main/java/com/bestfriend/danjjak/support/dto/SupportDtos.java) | `class` | 전화번호와 실제·모의·실패 알림 결과의 외부 표현 설계 |
| [support/mapper/SupportMapper.java](../../backend/src/main/java/com/bestfriend/danjjak/support/mapper/SupportMapper.java) | `interface` | 보호자 번호·알림 대상 판정·실제 성공 시각의 저장 및 조회 경계 |
| [support/model/GuardianContactRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/support/model/GuardianContactRecord.java) | `class` | 사용자별 보호자 전화번호 하나를 표현하는 업무 모델 |
| [support/model/NotificationAnomalyRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/support/model/NotificationAnomalyRecord.java) | `class` | 알림에 필요한 본인 판정·동의·미결정 상태의 조회 모델 |
| [support/service/GuardianNotificationService.java](../../backend/src/main/java/com/bestfriend/danjjak/support/service/GuardianNotificationService.java) | `class` | 높은 주의·동의·명시 선택 확인과 본인 전송·모의 결과 구분을 조정 |
| [support/service/KakaoMemoMessageClient.java](../../backend/src/main/java/com/bestfriend/danjjak/support/service/KakaoMemoMessageClient.java) | `class` | 카카오 나에게 보내기 제공사 요청과 실제 결과를 격리하는 구현 예정 역할 |
| [support/service/KakaoMessageClient.java](../../backend/src/main/java/com/bestfriend/danjjak/support/service/KakaoMessageClient.java) | `interface` | 본인 계정 메시지 전송 제공사의 협력 계약을 배치할 인터페이스 |
| [support/service/SupportService.java](../../backend/src/main/java/com/bestfriend/danjjak/support/service/SupportService.java) | `class` | 저장 보호자 번호 수정·조회와 제공된 고객센터 번호 조회를 조정 |
| [transfer/controller/TransferController.java](../../backend/src/main/java/com/bestfriend/danjjak/transfer/controller/TransferController.java) | `class` | 모의 송금 시도·판정 재확인·사용자 결정의 입출력 경계 |
| [transfer/dto/TransferDtos.java](../../backend/src/main/java/com/bestfriend/danjjak/transfer/dto/TransferDtos.java) | `class` | 선택한 계좌·금액·금융 결과·판정과 결정의 외부 표현 설계 |
| [transfer/mapper/TransferMapper.java](../../backend/src/main/java/com/bestfriend/danjjak/transfer/mapper/TransferMapper.java) | `interface` | 선택 내 계좌·거래·판정과 해당 실행 최종 확정의 저장 경계 |
| [transfer/model/AnomalyCommand.java](../../backend/src/main/java/com/bestfriend/danjjak/transfer/model/AnomalyCommand.java) | `class` | 한 이상 시도의 송금 정보와 전체 사유를 기록하는 저장 명령 |
| [transfer/model/AnomalyRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/transfer/model/AnomalyRecord.java) | `class` | 판정 재확인·최종 결정과 연결 거래를 보존하는 업무 모델 |
| [transfer/model/RecipientRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/transfer/model/RecipientRecord.java) | `class` | 등록 또는 직접 입력 받는 사람·계좌의 송금용 조회 모델 |
| [transfer/model/TransactionCommand.java](../../backend/src/main/java/com/bestfriend/danjjak/transfer/model/TransactionCommand.java) | `class` | 선택 내 계좌 차감과 함께 확정할 거래·당시 받는 정보의 저장 명령 |
| [transfer/model/TransferAccountRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/transfer/model/TransferAccountRecord.java) | `class` | 소유권·잔액·모의 PIN 비교에 필요한 출금 계좌 조회 모델 |
| [transfer/service/FdsEvaluator.java](../../backend/src/main/java/com/bestfriend/danjjak/transfer/service/FdsEvaluator.java) | `class` | 고액·완료 송금 반복 여부로 위험 단계와 전체 사유를 결정하는 도메인 역할 |
| [transfer/service/PinVerifier.java](../../backend/src/main/java/com/bestfriend/danjjak/transfer/service/PinVerifier.java) | `class` | 선택 내 계좌의 모의 비밀번호 비교 책임을 분리한 도메인 역할 |
| [transfer/service/TransferService.java](../../backend/src/main/java/com/bestfriend/danjjak/transfer/service/TransferService.java) | `class` | 소유권·PIN·잔액·FDS를 확인하고 차감·거래·마지막 방문·실행의 최종 결과를 조정 |
| [tts/controller/TtsController.java](../../backend/src/main/java/com/bestfriend/danjjak/tts/controller/TtsController.java) | `class` | 현재 문구와 선택 속도의 AI 음성 요청 입출력 경계 |
| [tts/dto/TtsDtos.java](../../backend/src/main/java/com/bestfriend/danjjak/tts/dto/TtsDtos.java) | `class` | AI 음성 합성 요청과 재생 결과의 외부 표현 설계 |
| [tts/service/OpenAiTtsClient.java](../../backend/src/main/java/com/bestfriend/danjjak/tts/service/OpenAiTtsClient.java) | `class` | AI 합성 제공사 요청과 실패를 격리하는 구현 예정 역할 |
| [tts/service/TtsClient.java](../../backend/src/main/java/com/bestfriend/danjjak/tts/service/TtsClient.java) | `interface` | 현재 문구·속도에 맞는 합성 제공사의 협력 계약을 배치할 인터페이스 |
| [tts/service/TtsService.java](../../backend/src/main/java/com/bestfriend/danjjak/tts/service/TtsService.java) | `class` | 현재 대본·속도의 합성과 실패 안내를 조정하고 금융 흐름과 분리 |
| [user/controller/UserController.java](../../backend/src/main/java/com/bestfriend/danjjak/user/controller/UserController.java) | `class` | 내 정보·접근성·독립 선택 동의의 입출력 경계 |
| [user/dto/UserDtos.java](../../backend/src/main/java/com/bestfriend/danjjak/user/dto/UserDtos.java) | `class` | 사용자·접근성·동의 완료 및 두 선택값의 외부 표현 설계 |
| [user/mapper/UserMapper.java](../../backend/src/main/java/com/bestfriend/danjjak/user/mapper/UserMapper.java) | `interface` | 카카오 연결·현재 설정·동의 선택과 완료 상태의 저장 경계 |
| [user/model/UserSettingsRecord.java](../../backend/src/main/java/com/bestfriend/danjjak/user/model/UserSettingsRecord.java) | `class` | 사용자 식별·현재 접근성·독립 동의를 표현하는 업무 모델 |
| [user/service/UserService.java](../../backend/src/main/java/com/bestfriend/danjjak/user/service/UserService.java) | `class` | 같은 카카오 사용자 연결·내 정보·설정·동의 저장을 조정 |
