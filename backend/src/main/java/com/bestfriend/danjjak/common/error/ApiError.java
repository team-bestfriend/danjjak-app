package com.bestfriend.danjjak.common.error;

import java.util.List;
import java.util.UUID;

public record ApiError(
        String code, String message, List<FieldError> fieldErrors, boolean retryable, String requestId) {

    public ApiError(String code, String message) {
        this(code, message, List.of(), false, UUID.randomUUID().toString());
    }

    public record FieldError(String field, String code, String message) {}
}
