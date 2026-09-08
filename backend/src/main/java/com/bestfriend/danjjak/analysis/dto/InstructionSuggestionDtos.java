package com.bestfriend.danjjak.analysis.dto;

import com.bestfriend.danjjak.analysis.dto.UsageAnalysisDtos.AnalysisStatus;
import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public final class InstructionSuggestionDtos {
    private InstructionSuggestionDtos() {}

    public record SuggestionResponse(AnalysisStatus status, LocalDate from, LocalDate to, InstructionSuggestion suggestion) {}

    public record InstructionSuggestion(long patternId, String patternTitle, long stepId, int stepOrder,
                                        String stepCode, String stepName, String currentText, String suggestedText,
                                        boolean hasFamilyAudio, boolean voiceScriptOutdated) {}

    public record ApplySuggestionRequest(@NotBlank @Size(max = 500) String expectedText) {}
}
