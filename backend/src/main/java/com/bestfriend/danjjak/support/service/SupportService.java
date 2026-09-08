package com.bestfriend.danjjak.support.service;

import com.bestfriend.danjjak.common.error.ApiException;
import com.bestfriend.danjjak.support.dto.SupportDtos;
import com.bestfriend.danjjak.support.dto.SupportDtos.CustomerCenterResponse;
import com.bestfriend.danjjak.support.dto.SupportDtos.GuardianContactRequest;
import com.bestfriend.danjjak.support.dto.SupportDtos.GuardianContactResponse;
import com.bestfriend.danjjak.support.dto.SupportDtos.SupportResponse;
import com.bestfriend.danjjak.support.mapper.SupportMapper;
import com.bestfriend.danjjak.support.model.GuardianContactRecord;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupportService {

    private final SupportMapper supportMapper;
    private final String customerCenterPhone;
    private final String customerCenterName;

    public SupportService(SupportMapper supportMapper, Environment environment) {
        this.supportMapper = supportMapper;
        this.customerCenterPhone =
                environment.getProperty("support.customer-center-phone", "");
        String configuredName = environment.getProperty("support.customer-center-name", "");
        this.customerCenterName = configuredName.isBlank() ? "고객센터" : configuredName;
    }

    @Transactional(readOnly = true)
    public SupportResponse getSupport(long userId) {
        requireConsent(userId);
        if (!validPhoneNumber(customerCenterPhone)) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "SUPPORT_NOT_CONFIGURED",
                    "고객센터 전화번호가 아직 설정되지 않았어요.");
        }
        return new SupportResponse(
                toResponse(supportMapper.findGuardian(userId)),
                new CustomerCenterResponse(customerCenterName, customerCenterPhone), "SELF");
    }

    @Transactional
    public GuardianContactResponse updateGuardian(
            long userId, GuardianContactRequest request) {
        requireConsent(userId);
        if (request == null || !validPhoneNumber(request.phoneNumber())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                    "전화번호는 5~30자의 숫자 또는 숫자 묶음 사이 하이픈으로 입력해 주세요.");
        }
        supportMapper.upsertGuardian(userId, request.phoneNumber());
        GuardianContactRecord saved = supportMapper.findGuardian(userId);
        if (saved == null || !request.phoneNumber().equals(saved.getPhoneNumber())) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "보호자 전화번호를 저장하지 못했어요.");
        }
        return toResponse(saved);
    }

    private GuardianContactResponse toResponse(GuardianContactRecord record) {
        return new GuardianContactResponse(record == null ? null : record.getPhoneNumber());
    }

    private boolean validPhoneNumber(String value) {
        return value != null && value.length() >= 5 && value.length() <= 30
                && value.matches(SupportDtos.PHONE_NUMBER_PATTERN);
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
}
