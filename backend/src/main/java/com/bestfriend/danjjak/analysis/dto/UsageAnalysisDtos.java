package com.bestfriend.danjjak.analysis.dto;

import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class UsageAnalysisDtos {

    private UsageAnalysisDtos() {}

    public enum AnalysisStatus {
        CONSENT_REQUIRED, CONSENT_DECLINED, NO_DATA, AVAILABLE
    }

    public record UsageAnalysisResponse(
        AnalysisStatus status, LocalDate from, LocalDate to,
        List<PatternUsageResponse> patterns, List<StepAnalysisResponse> steps,
        StepAnalysisResponse difficultStep) {}

    public record PatternUsageResponse(
        long patternId, PatternType patternType, String title, long completedCount) {}

    public record StepAnalysisResponse(
        long patternId, long stepId, String stepCode, String stepName, int stepOrder,
        long visitCount, long errorScore, BigDecimal averageDurationSeconds) {}
}
