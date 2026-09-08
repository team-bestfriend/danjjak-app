package com.bestfriend.danjjak.support.service;

import com.bestfriend.danjjak.common.error.ApiException;
import com.bestfriend.danjjak.support.dto.SupportDtos.NotificationRequest;
import com.bestfriend.danjjak.support.dto.SupportDtos.NotificationResponse;
import com.bestfriend.danjjak.support.mapper.SupportMapper;
import com.bestfriend.danjjak.support.model.NotificationAnomalyRecord;
import com.bestfriend.danjjak.support.service.KakaoMessageClient.KakaoSendResult;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuardianNotificationService {

    private final SupportMapper supportMapper;
    private final KakaoMessageClient kakaoMessageClient;
    private final Clock clock;

    public GuardianNotificationService(
            SupportMapper supportMapper, KakaoMessageClient kakaoMessageClient, Clock clock) {
        this.supportMapper = supportMapper;
        this.kakaoMessageClient = kakaoMessageClient;
        this.clock = clock;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public NotificationResponse notify(
            long userId, long anomalyEventId, String accessToken, NotificationRequest request) {
        requireConsent(userId);
        if (anomalyEventId <= 0 || request == null || !Boolean.TRUE.equals(request.confirmedSelfDemo())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                    "본인 카카오톡으로 보내는 시연임을 확인해 주세요.");
        }
        // 외부 호출 전에 판정 행을 잠가 중복 요청과 최종 결정의 순서를 확정합니다.
        NotificationAnomalyRecord anomaly = supportMapper.findAnomalyForUpdate(userId, anomalyEventId);
        if (anomaly == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "이상거래를 찾을 수 없습니다.");
        }
        if (!"HIGH".equals(anomaly.getRiskLevel()) || anomaly.getFinalAction() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "ANOMALY_NOT_ELIGIBLE",
                    "아직 결정하지 않은 높은 주의 거래에서만 알림을 보낼 수 있어요.");
        }
        if (!supportMapper.hasGuardianShareConsent(userId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "GUARDIAN_SHARE_CONSENT_REQUIRED",
                    "보호자 공유 동의 후 카카오 알림을 요청할 수 있습니다.");
        }
        if (anomaly.getGuardianNotifiedAt() != null) {
            return actualResponse(anomaly.getGuardianNotifiedAt());
        }

        if (accessToken == null || accessToken.isBlank()) {
            return noCredentialsResponse();
        }

        String message = createMessage(anomaly);
        KakaoSendResult result;
        try {
            result = kakaoMessageClient.sendToMe(accessToken, message);
        } catch (RuntimeException exception) {
            throw unknownResult();
        }
        if (result == null || KakaoMessageClient.RESULT_UNKNOWN.equals(result.detail())) {
            throw unknownResult();
        }
        if (result.success()) {
            // 기존 DATETIME 정밀도에 맞춰 UTC 초 단위 시각을 저장하고 재조회에도 같은 값을 반환합니다.
            LocalDateTime sentAt = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC).withNano(0);
            int updated;
            try {
                updated = supportMapper.markGuardianNotified(userId, anomalyEventId, sentAt);
            } catch (RuntimeException exception) {
                throw unknownResult();
            }
            if (updated != 1) {
                throw unknownResult();
            }
            return actualResponse(sentAt);
        }
        if (KakaoMessageClient.NOT_CONFIGURED.equals(result.detail())) {
            return noCredentialsResponse();
        }
        if (result.httpStatus() == null && !KakaoMessageClient.REQUEST_NOT_SENT.equals(result.detail())) {
            throw unknownResult();
        }
        return new NotificationResponse(
                "MOCK_AFTER_FAILURE", "카카오톡 전송에 실패해 모의 알림으로 시연했어요.", null, "SELF");
    }

    private NotificationResponse actualResponse(LocalDateTime sentAt) {
        return new NotificationResponse("ACTUAL", "시연 알림을 내 카카오톡으로 보냈어요.",
                sentAt.toInstant(ZoneOffset.UTC).toString(), "SELF");
    }

    private NotificationResponse noCredentialsResponse() {
        return new NotificationResponse("MOCK_NO_CREDENTIALS",
                "실제 전송 없이 알림 보내기를 시연했어요.", null, "SELF");
    }

    private ApiException unknownResult() {
        return new ApiException(HttpStatus.BAD_GATEWAY, "NOTIFICATION_RESULT_UNKNOWN",
                "카카오톡 전송 결과를 확인할 수 없어요. 내 카카오톡을 확인해 주세요. 자동으로 다시 보내지 않아요.");
    }

    private void requireConsent(long userId) {
        Boolean completed = supportMapper.findConsentCompleted(userId);
        if (completed == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "SESSION_REQUIRED", "로그인이 필요합니다.");
        }
        if (!completed) {
            throw new ApiException(HttpStatus.CONFLICT, "CONSENT_NOT_COMPLETED",
                    "동의 선택을 먼저 완료해 주세요. 선택 동의는 거절할 수 있어요.");
        }
    }

    private String createMessage(NotificationAnomalyRecord anomaly) {
        String amount =
                NumberFormat.getNumberInstance(Locale.KOREA).format(anomaly.getAmount()) + "원";
        StringBuilder message = new StringBuilder("[단짝 본인 전송 시연] 보호자 공유 알림을 로그인한 본인에게 보내는 시연입니다.");
        message.append("\n위험 정도: 높은 주의");
        if (anomaly.isHighAmountDetected()) {
            message.append("\n고액 사유: 1,000만원 이상 보내려고 해요.");
        }
        if (anomaly.isRepeatedTransferDetected()) {
            message.append("\n반복 사유: 최근 10분 동안 ").append(anomaly.getRecentTransferCount()).append("번 보냈어요.");
        }
        message.append("\n받는 사람: ").append(anomaly.getRecipientName())
                .append("\n받는 계좌: ").append(anomaly.getRecipientBankName()).append(" ")
                .append(maskAccountNumber(anomaly.getRecipientAccountNumber()))
                .append("\n금액: ").append(amount)
                .append("\n보호자의 확인이나 송금 승인을 의미하지 않아요. 앱에서 내용을 확인해 주세요.");
        return message.toString();
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null) {
            return "****";
        }
        String digits = accountNumber.replaceAll("[^0-9]", "");
        return digits.length() <= 4 ? "****" : "****-" + digits.substring(digits.length() - 4);
    }
}
