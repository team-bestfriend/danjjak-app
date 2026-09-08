package com.bestfriend.danjjak.support.controller;

import com.bestfriend.danjjak.common.error.ApiException;
import com.bestfriend.danjjak.common.session.DemoSessionUserResolver;
import com.bestfriend.danjjak.support.dto.SupportDtos.GuardianContactRequest;
import com.bestfriend.danjjak.support.dto.SupportDtos.GuardianContactResponse;
import com.bestfriend.danjjak.support.dto.SupportDtos.NotificationRequest;
import com.bestfriend.danjjak.support.dto.SupportDtos.NotificationResponse;
import com.bestfriend.danjjak.support.dto.SupportDtos.SupportResponse;
import com.bestfriend.danjjak.support.service.GuardianNotificationService;
import com.bestfriend.danjjak.support.service.SupportService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SupportController {

    private final SupportService supportService;
    private final GuardianNotificationService notificationService;
    private final DemoSessionUserResolver userResolver;

    public SupportController(
            SupportService supportService,
            GuardianNotificationService notificationService,
            DemoSessionUserResolver userResolver) {
        this.supportService = supportService;
        this.notificationService = notificationService;
        this.userResolver = userResolver;
    }

    @GetMapping("/support")
    public ResponseEntity<SupportResponse> getSupport(HttpServletRequest servletRequest) {
        return noStore(supportService.getSupport(
                userResolver.resolveUserId(servletRequest.getSession(false))));
    }

    @PutMapping("/support/guardian")
    public ResponseEntity<GuardianContactResponse> updateGuardian(
            @Valid @RequestBody GuardianContactRequest request,
            @RequestHeader(value = "X-CSRF-Token", required = false) String csrfToken,
            HttpServletRequest servletRequest) {
        long userId = userResolver.resolveUserId(servletRequest.getSession(false));
        userResolver.validateCsrfToken(servletRequest.getSession(false), csrfToken);
        return noStore(supportService.updateGuardian(userId, request));
    }

    @PostMapping("/anomaly-events/{anomalyEventId}/guardian-notification")
    public ResponseEntity<NotificationResponse> notifyGuardian(
            @PathVariable("anomalyEventId") String anomalyEventId,
            @Valid @RequestBody NotificationRequest request,
            @RequestHeader(value = "X-CSRF-Token", required = false) String csrfToken,
            HttpServletRequest servletRequest) {
        long userId = userResolver.resolveUserId(servletRequest.getSession(false));
        userResolver.validateCsrfToken(servletRequest.getSession(false), csrfToken);
        return noStore(notificationService.notify(
                userId, parseAnomalyId(anomalyEventId),
                userResolver.resolveKakaoAccessToken(servletRequest.getSession(false)), request));
    }

    private long parseAnomalyId(String value) {
        if (value != null && value.matches("^[1-9][0-9]{0,18}$")) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
                // 저장 가능한 식별자 범위를 넘으면 입력 오류로 처리합니다.
            }
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                "이상거래 식별자가 올바르지 않습니다.");
    }

    private <T> ResponseEntity<T> noStore(T response) {
        return ResponseEntity.ok().header("Cache-Control", "no-store").body(response);
    }
}
