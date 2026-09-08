package com.bestfriend.danjjak.pattern.controller;

import com.bestfriend.danjjak.common.session.DemoSessionUserResolver;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.ExecutionFinishRequest;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.ExecutionFinishResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.ExecutionStartRequest;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.ExecutionStartResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternCreateRequest;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternDetailResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternOrderRequest;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternSummaryResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternTemplateResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternUpdateRequest;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.StepVisitResponse;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.VisitStartRequest;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.VisitUpdateRequest;
import com.bestfriend.danjjak.pattern.service.PatternService;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PatternController {

    private final PatternService patternService;
    private final DemoSessionUserResolver userResolver;

    public PatternController(PatternService patternService, DemoSessionUserResolver userResolver) {
        this.patternService = patternService;
        this.userResolver = userResolver;
    }

    @GetMapping("/pattern-templates")
    public List<PatternTemplateResponse> getTemplates(HttpSession session) {
        userResolver.resolveUserId(session);
        return patternService.getTemplates();
    }

    @GetMapping("/patterns")
    public List<PatternSummaryResponse> getPatterns(HttpSession session) {
        return patternService.getPatterns(userResolver.resolveUserId(session));
    }

    @GetMapping("/patterns/{patternId}")
    public PatternDetailResponse getPattern(
        @PathVariable long patternId, HttpSession session) {
        return patternService.getPattern(userResolver.resolveUserId(session), patternId);
    }

    @PostMapping("/patterns")
    public ResponseEntity<PatternDetailResponse> createPattern(
        @Valid @RequestBody PatternCreateRequest request, HttpSession session) {
        PatternDetailResponse response =
            patternService.createPattern(userResolver.resolveUserId(session), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/patterns/{patternId}")
    public PatternDetailResponse updatePattern(
        @PathVariable long patternId,
        @Valid @RequestBody PatternUpdateRequest request,
        HttpSession session) {
        return patternService.updatePattern(
            userResolver.resolveUserId(session), patternId, request);
    }

    @PutMapping("/patterns/order")
    public List<PatternSummaryResponse> reorderPatterns(
        @Valid @RequestBody PatternOrderRequest request, HttpSession session) {
        return patternService.reorderPatterns(userResolver.resolveUserId(session), request);
    }

    @DeleteMapping("/patterns/{patternId}")
    public ResponseEntity<Void> deactivatePattern(
        @PathVariable long patternId, HttpSession session) {
        patternService.deactivatePattern(userResolver.resolveUserId(session), patternId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/patterns/{patternId}/executions")
    public ResponseEntity<ExecutionStartResponse> startExecution(
        @PathVariable long patternId,
        @Valid @RequestBody(required = false) ExecutionStartRequest request,
        HttpSession session) {
        ExecutionStartResponse response =
            patternService.startExecution(
                userResolver.resolveUserId(session), patternId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/pattern-executions/{executionId}/visits")
    public ResponseEntity<StepVisitResponse> startVisit(
        @PathVariable long executionId,
        @Valid @RequestBody VisitStartRequest request,
        HttpSession session) {
        StepVisitResponse response =
            patternService.startVisit(
                userResolver.resolveUserId(session), executionId, request);
        return response == null ? ResponseEntity.noContent().build()
            : ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/pattern-executions/{executionId}/visits/{visitId}")
    public ResponseEntity<StepVisitResponse> updateVisit(
        @PathVariable long executionId,
        @PathVariable long visitId,
        @Valid @RequestBody VisitUpdateRequest request,
        HttpSession session) {
        StepVisitResponse response = patternService.updateVisit(
            userResolver.resolveUserId(session), executionId, visitId, request);
        return response == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @PatchMapping("/pattern-executions/{executionId}")
    public ExecutionFinishResponse finishExecution(
        @PathVariable long executionId,
        @Valid @RequestBody ExecutionFinishRequest request,
        HttpSession session) {
        return patternService.finishExecution(
            userResolver.resolveUserId(session), executionId, request);
    }
}
