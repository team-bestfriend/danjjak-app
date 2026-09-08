package com.bestfriend.danjjak.pattern.dto;

import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

public final class PatternDtos {

    private PatternDtos() {}

    public enum PatternType {
        TRANSFER,
        PENSION_CHECK,
        MANAGEMENT_FEE_CHECK,
        BALANCE_CHECK,
        TRANSACTION_HISTORY,
        CUSTOMER_CENTER,
        UTILITY_BILL_CHECK,
        AUTO_TRANSFER_CHECK,
        CARD_HISTORY,
        DEPOSIT_MATURITY_CHECK
    }

    public enum ExecutionStatus {
        COMPLETED,
        CANCELLED,
        FAILED
    }

    public record PatternTemplateResponse(
        PatternType patternType,
        String defaultTitle,
        String defaultDescription,
        boolean requiresLinkedAccount,
        boolean available,
        List<TemplateStepResponse> steps) {}

    public record TemplateStepResponse(
        int stepOrder,
        String stepCode,
        String stepName,
        String instructionText,
        String screenCode,
        String targetElementId) {}

    public record PatternLinkedAccountResponse(
        long accountId,
        String bankCode,
        String bankName,
        String accountNumber,
        String accountAlias,
        Long registeredPersonId,
        String registeredPersonName,
        String relationship) {}

    public record PatternSummaryResponse(
        long patternId,
        int shortcutNumber,
        PatternType patternType,
        String title,
        String description,
        PatternLinkedAccountResponse linkedAccount) {}

    public record PatternStepResponse(
        long stepId,
        int stepOrder,
        String stepCode,
        String stepName,
        String instructionText,
        String screenCode,
        String targetElementId,
        String voiceFilePath,
        String voiceContentType) {}

    public record PatternDetailResponse(
        long patternId,
        int shortcutNumber,
        PatternType patternType,
        String title,
        String description,
        PatternLinkedAccountResponse linkedAccount,
        List<PatternStepResponse> steps) {}

    public record StepInstructionRequest(
        @NotBlank @Size(max = 50) String stepCode,
        @NotBlank @Size(max = 500) String instructionText) {}

    public record PatternCreateRequest(
        @NotNull PatternType patternType,
        @NotNull @Min(1) @Max(12) Integer shortcutNumber,
        @Size(min = 1, max = 100) String title,
        @Size(min = 1, max = 500) String description,
        @Positive Long linkedBankAccountId,
        List<@Valid StepInstructionRequest> stepInstructions) {}

    public record PatternUpdateRequest(
        @Size(min = 1, max = 100) String title,
        @Size(min = 1, max = 500) String description,
        @Positive Long linkedBankAccountId,
        List<@Valid StepInstructionRequest> stepInstructions) {}

    public record PatternOrderRequest(
        @NotEmpty @Size(max = 12) List<@Valid PatternOrderItem> items) {}

    public record PatternOrderItem(
        @Positive long patternId, @Min(1) @Max(12) int shortcutNumber) {}

    public record ExecutionStartRequest(@Positive Long sourceBankAccountId) {}

    public record ExecutionStartResponse(
        boolean loggingEnabled,
        Long executionId,
        LocalDateTime startedAt,
        PatternDetailResponse pattern) {}

    public record VisitStartRequest(@NotNull @Positive Long stepId) {}

    public record VisitUpdateRequest(
        @Min(0) Integer retryCount,
        @Min(0) Integer backCount,
        @Min(0) Integer wrongTouchCount,
        Boolean routeDeviation,
        Boolean completed) {}

    public record StepVisitResponse(
        long visitId,
        long executionId,
        long stepId,
        int visitNumber,
        int retryCount,
        int backCount,
        int wrongTouchCount,
        boolean routeDeviation,
        boolean completed,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Long durationSeconds) {}

    public record ExecutionFinishRequest(@NotNull ExecutionStatus status) {}

    public record ExecutionFinishResponse(
        long executionId, ExecutionStatus status, LocalDateTime endedAt) {}
}
