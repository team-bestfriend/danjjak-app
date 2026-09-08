package com.bestfriend.danjjak.analysis.service;

import com.bestfriend.danjjak.analysis.dto.InstructionSuggestionDtos.ApplySuggestionRequest;
import com.bestfriend.danjjak.analysis.dto.InstructionSuggestionDtos.InstructionSuggestion;
import com.bestfriend.danjjak.analysis.dto.InstructionSuggestionDtos.SuggestionResponse;
import com.bestfriend.danjjak.common.error.ApiException;
import com.bestfriend.danjjak.pattern.dto.GuidanceDtos.GuidanceResponse;
import com.bestfriend.danjjak.pattern.dto.GuidanceDtos.GuidanceUpdateRequest;
import com.bestfriend.danjjak.pattern.dto.GuidanceDtos.VoiceMode;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternDetailResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternStepResponse;
import com.bestfriend.danjjak.pattern.service.GuidanceService;
import com.bestfriend.danjjak.pattern.service.PatternService;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstructionSuggestionService {
    private final UsageAnalysisService analysis;
    private final PatternService patterns;
    private final GuidanceService guidance;

    public InstructionSuggestionService(UsageAnalysisService analysis, PatternService patterns, GuidanceService guidance) {
        this.analysis = analysis;
        this.patterns = patterns;
        this.guidance = guidance;
    }

    public SuggestionResponse get(long userId, LocalDate from, LocalDate to) {
        var report = analysis.getUsageAnalysis(userId, from, to);
        var difficult = report.difficultStep();
        if (difficult == null) return new SuggestionResponse(report.status(), from, to, null);
        var pattern = patterns.getPattern(userId, difficult.patternId());
        var step = findStep(pattern, difficult.stepId());
        var current = findGuidance(userId, pattern.patternId(), step.stepCode());
        String text = suggestedText(pattern, step);
        var suggestion = text == null ? null : new InstructionSuggestion(pattern.patternId(), pattern.title(), step.stepId(),
            step.stepOrder(), step.stepCode(), step.stepName(), current.text(), text,
            current.audioUrl() != null, current.voiceScriptOutdated());
        return new SuggestionResponse(report.status(), from, to, suggestion);
    }

    @Transactional
    public GuidanceResponse apply(long userId, long patternId, long stepId, ApplySuggestionRequest request) {
        var pattern = patterns.getPattern(userId, patternId);
        var step = findStep(pattern, stepId);
        var current = findGuidance(userId, patternId, step.stepCode());
        if (!current.text().equals(request.expectedText())) {
            throw new ApiException(HttpStatus.CONFLICT, "INSTRUCTION_CHANGED", "안내 문구가 바뀌었어요. 다시 불러와 비교한 뒤 적용해 주세요.");
        }
        String text = suggestedText(pattern, step);
        if (text == null) throw missing();
        // 수동 편집과 같은 저장 경로를 사용해 음성 선택과 기존 녹음을 보존한다.
        return guidance.update(userId, patternId, step.stepCode(), new GuidanceUpdateRequest(text,
            current.voiceMode() == null ? null : VoiceMode.valueOf(current.voiceMode())));
    }

    private GuidanceResponse findGuidance(long userId, long patternId, String stepCode) {
        return guidance.getAll(userId, patternId).stream().filter(item -> item.target().equals(stepCode))
            .findFirst().orElseThrow(InstructionSuggestionService::missing);
    }

    private PatternStepResponse findStep(PatternDetailResponse pattern, long stepId) {
        return pattern.steps().stream().filter(step -> step.stepId() == stepId)
            .findFirst().orElseThrow(InstructionSuggestionService::missing);
    }

    private String suggestedText(PatternDetailResponse pattern, PatternStepResponse step) {
        return switch (step.stepCode()) {
            case "SELECT_SOURCE" -> "돈을 보낼 내 통장을 눌러 주세요. 고른 통장에서 돈이 나가요.";
            case "SELECT_PERSON" -> "돈을 받을 가족의 이름을 눌러 주세요.";
            case "SELECT_ACCOUNT" -> "돈을 받을 계좌를 눌러 주세요. 이름과 계좌번호를 함께 확인해요.";
            case "INPUT_AMOUNT" -> "보낼 금액을 숫자로 눌러 주세요. 다 입력하면 다음을 눌러 주세요.";
            case "CONFIRM_TRANSFER" -> "받는 분과 금액이 맞는지 천천히 확인해 주세요.";
            case "ENTER_PIN" -> "돈을 보낼 내 통장의 비밀번호 네 자리를 눌러 주세요.";
            case "CALL_SUPPORT" -> "도움이 필요하면 전화 연결하기를 눌러 주세요.";
            case "CHECK_RESULT" -> switch (pattern.patternType()) {
                case BALANCE_CHECK -> "잔액 보기를 누르면 이 통장에 남은 돈을 볼 수 있어요.";
                case PENSION_CHECK -> "이 계좌에 들어온 연금 내역을 천천히 확인해 주세요.";
                case MANAGEMENT_FEE_CHECK -> "이 계좌의 관리비 거래 내역을 천천히 확인해 주세요.";
                case UTILITY_BILL_CHECK -> "이 계좌의 공과금 거래 내역을 천천히 확인해 주세요.";
                case CUSTOMER_CENTER -> "도움이 필요하면 전화 연결하기를 눌러 주세요.";
                default -> "이 계좌에서 들어오고 나간 돈을 천천히 확인해 주세요.";
            };
            default -> null;
        };
    }

    private static ApiException missing() {
        return new ApiException(HttpStatus.NOT_FOUND, "SUGGESTION_NOT_FOUND", "이 단계의 문구 제안을 찾을 수 없습니다.");
    }
}
