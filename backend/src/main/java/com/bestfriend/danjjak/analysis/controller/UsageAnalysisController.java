package com.bestfriend.danjjak.analysis.controller;

import com.bestfriend.danjjak.analysis.dto.UsageAnalysisDtos.UsageAnalysisResponse;
import com.bestfriend.danjjak.analysis.service.UsageAnalysisService;
import com.bestfriend.danjjak.common.session.DemoSessionUserResolver;
import java.time.LocalDate;
import javax.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usage-analysis")
public class UsageAnalysisController {

    private final UsageAnalysisService analysisService;
    private final DemoSessionUserResolver userResolver;

    public UsageAnalysisController(UsageAnalysisService analysisService, DemoSessionUserResolver userResolver) {
        this.analysisService = analysisService;
        this.userResolver = userResolver;
    }

    @GetMapping
    public UsageAnalysisResponse getUsageAnalysis(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        HttpSession session) {
        return analysisService.getUsageAnalysis(userResolver.resolveUserId(session), from, to);
    }
}
