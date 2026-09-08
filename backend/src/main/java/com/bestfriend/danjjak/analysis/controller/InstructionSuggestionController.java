package com.bestfriend.danjjak.analysis.controller;

import com.bestfriend.danjjak.analysis.dto.InstructionSuggestionDtos.ApplySuggestionRequest;
import com.bestfriend.danjjak.analysis.dto.InstructionSuggestionDtos.SuggestionResponse;
import com.bestfriend.danjjak.analysis.service.InstructionSuggestionService;
import com.bestfriend.danjjak.common.session.DemoSessionUserResolver;
import com.bestfriend.danjjak.pattern.dto.GuidanceDtos.GuidanceResponse;
import java.time.LocalDate;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InstructionSuggestionController {
    private final InstructionSuggestionService suggestions;
    private final DemoSessionUserResolver users;

    public InstructionSuggestionController(InstructionSuggestionService suggestions, DemoSessionUserResolver users) {
        this.suggestions = suggestions;
        this.users = users;
    }

    @GetMapping("/api/usage-analysis/instruction-suggestion")
    public SuggestionResponse get(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to, HttpSession session) {
        return suggestions.get(users.resolveUserId(session), from, to);
    }

    @PostMapping("/api/patterns/{patternId}/steps/{stepId}/instruction-suggestion")
    public GuidanceResponse apply(@PathVariable long patternId, @PathVariable long stepId,
                                  @Valid @RequestBody ApplySuggestionRequest request, HttpSession session) {
        return suggestions.apply(users.resolveUserId(session), patternId, stepId, request);
    }
}

