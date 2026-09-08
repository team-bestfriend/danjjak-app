package com.bestfriend.danjjak.pattern.service;

import com.bestfriend.danjjak.common.error.ApiException;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.ExecutionFinishRequest;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.ExecutionFinishResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.ExecutionStartRequest;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.ExecutionStartResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternCreateRequest;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternDetailResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternLinkedAccountResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternOrderItem;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternOrderRequest;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternStepResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternSummaryResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternTemplateResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternType;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternUpdateRequest;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.StepInstructionRequest;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.StepVisitResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.TemplateStepResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.VisitStartRequest;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.VisitUpdateRequest;
import com.bestfriend.danjjak.pattern.mapper.PatternMapper;
import com.bestfriend.danjjak.pattern.model.ExecutionCommand;
import com.bestfriend.danjjak.pattern.model.PatternCommand;
import com.bestfriend.danjjak.pattern.model.PatternRecord;
import com.bestfriend.danjjak.pattern.model.PatternStepRecord;
import com.bestfriend.danjjak.pattern.model.StepCommand;
import com.bestfriend.danjjak.pattern.model.StepVisitRecord;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatternService {

    private static final int MAX_ACTIVE_PATTERNS = 12;

    private final PatternMapper patternMapper;
    private final PatternCatalog patternCatalog;
    private final Clock clock;

    public PatternService(PatternMapper patternMapper, PatternCatalog patternCatalog, Clock clock) {
        this.patternMapper = patternMapper;
        this.patternCatalog = patternCatalog;
        this.clock = clock;
    }

    public List<PatternTemplateResponse> getTemplates() {
        return patternCatalog.getTemplates();
    }

    public List<PatternSummaryResponse> getPatterns(long userId) {
        return patternMapper.findActivePatterns(userId).stream().map(this::toSummary).toList();
    }

    public PatternDetailResponse getPattern(long userId, long patternId) {
        PatternRecord pattern = requirePattern(userId, patternId);
        return toDetail(pattern, patternMapper.findPatternSteps(patternId));
    }

    @Transactional
    public PatternDetailResponse createPattern(long userId, PatternCreateRequest request) {
        PatternTemplateResponse template = requireAvailableTemplate(request.patternType());
        List<Long> activeIds = patternMapper.findActivePatternIdsForUpdate(userId);
        if (activeIds.size() >= MAX_ACTIVE_PATTERNS) {
            throw conflict("PATTERN_LIMIT_REACHED", "활성 단축번호는 최대 12개까지 등록할 수 있습니다.");
        }
        if (patternMapper.countShortcut(userId, request.shortcutNumber()) > 0) {
            throw conflict("SHORTCUT_ALREADY_USED", "이미 사용 중인 단축번호입니다.");
        }

        Long linkedAccountId = resolveLinkedAccount(userId, request.patternType(), request.linkedBankAccountId());
        PatternCommand command = new PatternCommand();
        command.setUserId(userId);
        command.setShortcutNumber(request.shortcutNumber());
        command.setPatternType(request.patternType().name());
        command.setTitle(valueOrDefault(request.title(), template.defaultTitle()));
        command.setDescription(valueOrDefault(request.description(), template.defaultDescription()));
        command.setLinkedAccountId(linkedAccountId);
        patternMapper.insertPattern(command);

        Map<String, String> overrides = instructionOverrides(template.steps(), request.stepInstructions());
        for (TemplateStepResponse templateStep : template.steps()) {
            StepCommand step = new StepCommand();
            step.setPatternId(command.getPatternId());
            step.setStepOrder(templateStep.stepOrder());
            step.setStepCode(templateStep.stepCode());
            step.setStepName(templateStep.stepName());
            step.setInstructionText(overrides.getOrDefault(templateStep.stepCode(), templateStep.instructionText()));
            step.setScreenCode(templateStep.screenCode());
            step.setTargetElementId(templateStep.targetElementId());
            patternMapper.insertPatternStep(step);
        }
        return getPattern(userId, command.getPatternId());
    }

    @Transactional
    public PatternDetailResponse updatePattern(
        long userId, long patternId, PatternUpdateRequest request) {
        PatternRecord existing = requirePattern(userId, patternId);
        boolean hasInstructions = request.stepInstructions() != null && !request.stepInstructions().isEmpty();
        if (request.title() == null
            && request.description() == null
            && request.linkedBankAccountId() == null
            && !hasInstructions) {
            throw badRequest("EMPTY_PATTERN_UPDATE", "수정할 패턴 정보를 한 가지 이상 보내야 합니다.");
        }

        PatternType type = PatternType.valueOf(existing.getPatternType());
        Long linkedAccountId = existing.getLinkedAccountId();
        if (request.linkedBankAccountId() != null) {
            linkedAccountId = resolveLinkedAccount(userId, type, request.linkedBankAccountId());
        }

        PatternCommand command = new PatternCommand();
        command.setPatternId(patternId);
        command.setUserId(userId);
        command.setTitle(valueOrDefault(request.title(), existing.getTitle()));
        command.setDescription(valueOrDefault(request.description(), existing.getDescription()));
        command.setLinkedAccountId(linkedAccountId);
        patternMapper.updatePattern(command);

        if (hasInstructions) {
            Set<String> seen = new HashSet<>();
            for (StepInstructionRequest instruction : request.stepInstructions()) {
                String code = instruction.stepCode().trim();
                if (!seen.add(code)) {
                    throw badRequest("DUPLICATE_STEP_INSTRUCTION", "같은 단계 안내를 두 번 수정할 수 없습니다.");
                }
                int updated =
                    patternMapper.updateStepInstruction(
                        userId, patternId, code, instruction.instructionText().trim());
                if (updated != 1) {
                    throw badRequest("PATTERN_STEP_NOT_FOUND", "수정할 패턴 단계를 찾을 수 없습니다.");
                }
            }
        }
        return getPattern(userId, patternId);
    }

    @Transactional
    public List<PatternSummaryResponse> reorderPatterns(long userId, PatternOrderRequest request) {
        List<Long> activeIds = patternMapper.findActivePatternIdsForUpdate(userId);
        if (request.items().size() != activeIds.size()) {
            throw badRequest("INCOMPLETE_PATTERN_ORDER", "현재 활성 패턴 전체의 순서를 보내야 합니다.");
        }

        Set<Long> requestedIds = new HashSet<>();
        Set<Integer> shortcutNumbers = new HashSet<>();
        for (PatternOrderItem item : request.items()) {
            if (!requestedIds.add(item.patternId()) || !shortcutNumbers.add(item.shortcutNumber())) {
                throw badRequest("DUPLICATE_PATTERN_ORDER", "패턴과 단축번호는 각각 한 번만 보낼 수 있습니다.");
            }
        }
        if (!requestedIds.equals(new HashSet<>(activeIds))) {
            throw badRequest("INCOMPLETE_PATTERN_ORDER", "현재 활성 패턴 전체의 순서를 보내야 합니다.");
        }

        patternMapper.clearActiveShortcuts(userId);
        for (PatternOrderItem item : request.items()) {
            if (patternMapper.activatePatternAtShortcut(userId, item.patternId(), item.shortcutNumber()) != 1) {
                throw conflict("PATTERN_ORDER_CONFLICT", "패턴 순서를 저장하지 못했습니다.");
            }
        }
        return getPatterns(userId);
    }

    @Transactional
    public void deactivatePattern(long userId, long patternId) {
        if (patternMapper.deactivatePattern(userId, patternId) != 1) {
            throw notFound("PATTERN_NOT_FOUND", "금융 패턴을 찾을 수 없습니다.");
        }
    }

    @Transactional
    public ExecutionStartResponse startExecution(
        long userId, long patternId, ExecutionStartRequest request) {
        PatternDetailResponse detail = getPattern(userId, patternId);
        Long sourceAccountId = request == null ? null : request.sourceBankAccountId();
        if (sourceAccountId != null && patternMapper.countOwnedSourceAccount(userId, sourceAccountId) != 1) {
            throw badRequest("INVALID_SOURCE_ACCOUNT", "실행에 사용할 본인 계좌를 찾을 수 없습니다.");
        }
        if (!patternMapper.isUsageLogAgreed(userId)) {
            return new ExecutionStartResponse(false, null, null, detail);
        }

        ExecutionCommand command = new ExecutionCommand();
        command.setPatternId(patternId);
        command.setSourceAccountId(sourceAccountId);
        command.setStartedAt(now());
        patternMapper.insertExecution(command);
        return new ExecutionStartResponse(
            true, command.getExecutionId(), command.getStartedAt(), detail);
    }

    @Transactional
    public StepVisitResponse startVisit(
        long userId, long executionId, VisitStartRequest request) {
        if (!patternMapper.isUsageLogAgreed(userId)) return null;
        requireStartedExecution(userId, executionId);
        if (patternMapper.countExecutionStep(userId, executionId, request.stepId()) != 1) {
            throw badRequest("INVALID_EXECUTION_STEP", "진입할 수 있는 실행 단계가 아닙니다.");
        }
        StepVisitRecord visit = new StepVisitRecord();
        visit.setExecutionId(executionId);
        visit.setStepId(request.stepId());
        // 잠금 대기 전에 만들어진 MySQL 스냅샷 대신 최신 방문번호를 잠금 조회한다.
        Integer nextVisitNumber = patternMapper.nextVisitNumber(executionId, request.stepId());
        int visitNumber = nextVisitNumber == null ? 1 : nextVisitNumber;
        if (visitNumber > 65535) {
            throw conflict("VISIT_LIMIT_REACHED", "이 단계의 방문 기록 한도에 도달했습니다.");
        }
        visit.setVisitNumber(visitNumber);
        visit.setStartedAt(now());
        patternMapper.closeOpenVisits(executionId, visit.getStartedAt());
        patternMapper.insertStepVisit(visit);
        return toVisitResponse(requireVisit(userId, executionId, visit.getVisitId()));
    }

    @Transactional
    public StepVisitResponse updateVisit(
        long userId,
        long executionId,
        long visitId,
        VisitUpdateRequest request) {
        validateVisitUpdate(request);
        if (!patternMapper.isUsageLogAgreed(userId)) return null;
        requireStartedExecution(userId, executionId);
        StepVisitRecord existing =
            patternMapper.findStepVisitForUpdate(userId, executionId, visitId);
        if (existing == null) {
            throw notFound("STEP_VISIT_NOT_FOUND", "단계 방문 기록을 찾을 수 없습니다.");
        }
        if (existing.getEndedAt() != null) {
            throw conflict("STEP_VISIT_ALREADY_FINISHED", "이미 종료된 단계 방문입니다.");
        }
        boolean completed = Boolean.TRUE.equals(request.completed());
        int retryCount = valueOrCurrent(request.retryCount(), existing.getRetryCount());
        int backCount = valueOrCurrent(request.backCount(), existing.getBackCount());
        int wrongTouchCount = valueOrCurrent(request.wrongTouchCount(), existing.getWrongTouchCount());
        boolean routeDeviation = Boolean.TRUE.equals(request.routeDeviation()) || existing.isRouteDeviation();
        // 완료 여부를 명시하면 정상 완료 또는 미완료 이탈로 방문을 종료한다.
        LocalDateTime visitEndedAt = request.completed() == null ? null : now();
        patternMapper.updateStepVisit(
            visitId,
            retryCount,
            backCount,
            wrongTouchCount,
            routeDeviation,
            completed,
            visitEndedAt);
        return toVisitResponse(requireVisit(userId, executionId, visitId));
    }

    @Transactional
    public ExecutionFinishResponse finishExecution(
        long userId, long executionId, ExecutionFinishRequest request) {
        if (request == null || request.status() == null) {
            throw badRequest("INVALID_REQUEST", "종료 상태가 필요합니다.");
        }
        requireStartedExecution(userId, executionId);
        String requestedStatus = request.status().name();
        LocalDateTime endedAt = now();
        patternMapper.closeOpenVisits(executionId, endedAt);
        if (patternMapper.finishExecution(userId, executionId, requestedStatus, endedAt) != 1) {
            throw conflict("EXECUTION_ALREADY_FINISHED", "이미 종료된 패턴 실행입니다.");
        }
        return new ExecutionFinishResponse(executionId, request.status(), endedAt);
    }

    private void requireStartedExecution(long userId, long executionId) {
        // 실행 행 잠금을 먼저 획득해 방문번호 계산과 종료·행동 요청을 직렬화한다.
        String status = patternMapper.findExecutionStatusForUpdate(userId, executionId);
        if (status == null) {
            throw notFound("PATTERN_EXECUTION_NOT_FOUND", "패턴 실행 기록을 찾을 수 없습니다.");
        }
        if (!"STARTED".equals(status)) {
            throw conflict("EXECUTION_ALREADY_FINISHED", "이미 종료된 패턴 실행입니다.");
        }
    }

    private void validateVisitUpdate(VisitUpdateRequest request) {
        if (request == null || (request.retryCount() == null && request.backCount() == null
            && request.wrongTouchCount() == null && request.routeDeviation() == null
            && request.completed() == null)) {
            throw badRequest("INVALID_REQUEST", "저장할 행동 또는 종료 여부가 필요합니다.");
        }
        if ((request.retryCount() != null && request.retryCount() < 0)
            || (request.backCount() != null && request.backCount() < 0)
            || (request.wrongTouchCount() != null && request.wrongTouchCount() < 0)) {
            throw badRequest("INVALID_REQUEST", "행동 횟수는 음수일 수 없습니다.");
        }
    }

    private PatternTemplateResponse requireAvailableTemplate(PatternType type) {
        PatternTemplateResponse template = patternCatalog.get(type);
        if (template == null || !template.available()) {
            throw conflict("PATTERN_TEMPLATE_UNAVAILABLE", "현재 등록할 수 없는 금융 업무입니다.");
        }
        return template;
    }

    private Long resolveLinkedAccount(long userId, PatternType type, Long requestedAccountId) {
        if (type == PatternType.TRANSFER) {
            if (requestedAccountId == null
                || patternMapper.countRegisteredRecipientAccount(userId, requestedAccountId) != 1) {
                throw badRequest("TRANSFER_RECIPIENT_REQUIRED", "송금 패턴에는 등록된 받는 계좌가 필요합니다.");
            }
            return requestedAccountId;
        }
        if (requestedAccountId != null) {
            if (patternMapper.countUserAccount(userId, requestedAccountId) != 1) {
                throw badRequest("LINKED_ACCOUNT_NOT_FOUND", "연결할 계좌를 찾을 수 없습니다.");
            }
            return requestedAccountId;
        }
        if (type == PatternType.CUSTOMER_CENTER) return null;
        Long primaryAccountId = patternMapper.findPrimaryAccountId(userId);
        if (primaryAccountId == null) {
            throw notFound("SOURCE_ACCOUNT_NOT_FOUND", "연결할 본인 계좌를 찾을 수 없습니다.");
        }
        return primaryAccountId;
    }

    private Map<String, String> instructionOverrides(
        List<TemplateStepResponse> steps, List<StepInstructionRequest> requested) {
        if (requested == null || requested.isEmpty()) return Map.of();
        Set<String> templateCodes = steps.stream().map(TemplateStepResponse::stepCode).collect(java.util.stream.Collectors.toSet());
        Map<String, String> overrides = new HashMap<>();
        for (StepInstructionRequest instruction : requested) {
            String code = instruction.stepCode().trim();
            if (!templateCodes.contains(code)) {
                throw badRequest("PATTERN_STEP_NOT_FOUND", "템플릿에 없는 단계 안내입니다.");
            }
            if (overrides.put(code, instruction.instructionText().trim()) != null) {
                throw badRequest("DUPLICATE_STEP_INSTRUCTION", "같은 단계 안내를 두 번 지정할 수 없습니다.");
            }
        }
        return overrides;
    }

    private PatternRecord requirePattern(long userId, long patternId) {
        PatternRecord pattern = patternMapper.findActivePattern(userId, patternId);
        if (pattern == null) throw notFound("PATTERN_NOT_FOUND", "금융 패턴을 찾을 수 없습니다.");
        return pattern;
    }

    private StepVisitRecord requireVisit(long userId, long executionId, long visitId) {
        StepVisitRecord visit = patternMapper.findStepVisit(userId, executionId, visitId);
        if (visit == null) throw notFound("STEP_VISIT_NOT_FOUND", "단계 방문 기록을 찾을 수 없습니다.");
        return visit;
    }

    private PatternSummaryResponse toSummary(PatternRecord pattern) {
        return new PatternSummaryResponse(
            pattern.getPatternId(),
            pattern.getShortcutNumber(),
            PatternType.valueOf(pattern.getPatternType()),
            pattern.getTitle(),
            pattern.getDescription(),
            toLinkedAccount(pattern));
    }

    private PatternDetailResponse toDetail(PatternRecord pattern, List<PatternStepRecord> steps) {
        return new PatternDetailResponse(
            pattern.getPatternId(),
            pattern.getShortcutNumber(),
            PatternType.valueOf(pattern.getPatternType()),
            pattern.getTitle(),
            pattern.getDescription(),
            toLinkedAccount(pattern),
            steps.stream().map(this::toStep).toList());
    }

    private PatternLinkedAccountResponse toLinkedAccount(PatternRecord pattern) {
        if (pattern.getLinkedAccountId() == null) return null;
        return new PatternLinkedAccountResponse(
            pattern.getLinkedAccountId(),
            pattern.getBankCode(),
            pattern.getBankName(),
            pattern.getAccountNumber(),
            pattern.getAccountAlias(),
            pattern.getRegisteredPersonId(),
            pattern.getRegisteredPersonName(),
            pattern.getRelationship());
    }

    private PatternStepResponse toStep(PatternStepRecord step) {
        return new PatternStepResponse(
            step.getStepId(),
            step.getStepOrder(),
            step.getStepCode(),
            step.getStepName(),
            step.getInstructionText(),
            step.getScreenCode(),
            step.getTargetElementId(),
            step.getVoiceFilePath(),
            step.getVoiceContentType());
    }

    private StepVisitResponse toVisitResponse(StepVisitRecord visit) {
        Long duration = visit.getEndedAt() == null
            ? null
            : Duration.between(visit.getStartedAt(), visit.getEndedAt()).getSeconds();
        return new StepVisitResponse(
            visit.getVisitId(),
            visit.getExecutionId(),
            visit.getStepId(),
            visit.getVisitNumber(),
            visit.getRetryCount(),
            visit.getBackCount(),
            visit.getWrongTouchCount(),
            visit.isRouteDeviation(),
            visit.isCompleted(),
            visit.getStartedAt(),
            visit.getEndedAt(),
            duration);
    }

    private int valueOrCurrent(Integer value, int current) {
        return value == null ? current : value;
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null ? fallback : value.trim();
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock).withNano(0);
    }

    private ApiException badRequest(String code, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, code, message);
    }

    private ApiException notFound(String code, String message) {
        return new ApiException(HttpStatus.NOT_FOUND, code, message);
    }

    private ApiException conflict(String code, String message) {
        return new ApiException(HttpStatus.CONFLICT, code, message);
    }
}
