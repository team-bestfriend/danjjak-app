package com.bestfriend.danjjak.analysis.service;

import com.bestfriend.danjjak.analysis.dto.UsageAnalysisDtos.AnalysisStatus;
import com.bestfriend.danjjak.analysis.dto.UsageAnalysisDtos.PatternUsageResponse;
import com.bestfriend.danjjak.analysis.dto.UsageAnalysisDtos.StepAnalysisResponse;
import com.bestfriend.danjjak.analysis.dto.UsageAnalysisDtos.UsageAnalysisResponse;
import com.bestfriend.danjjak.analysis.mapper.UsageAnalysisMapper;
import com.bestfriend.danjjak.analysis.model.StepAnalysisRecord;
import com.bestfriend.danjjak.common.error.ApiException;
import com.bestfriend.danjjak.pattern.dto.PatternDtos.PatternType;
import com.bestfriend.danjjak.user.mapper.UserMapper;
import com.bestfriend.danjjak.user.model.UserSettingsRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UsageAnalysisService {

    private static final Comparator<StepAnalysisResponse> DIFFICULTY_ORDER =
        Comparator.comparingLong(StepAnalysisResponse::errorScore).reversed()
            .thenComparing(StepAnalysisResponse::averageDurationSeconds,
                Comparator.nullsLast(Comparator.<BigDecimal>reverseOrder()))
            .thenComparingInt(StepAnalysisResponse::stepOrder)
            .thenComparingLong(StepAnalysisResponse::stepId);

    private final UsageAnalysisMapper analysisMapper;
    private final UserMapper userMapper;

    public UsageAnalysisService(UsageAnalysisMapper analysisMapper, UserMapper userMapper) {
        this.analysisMapper = analysisMapper;
        this.userMapper = userMapper;
    }

    public UsageAnalysisResponse getUsageAnalysis(long userId, LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)
            || from.getYear() < 1000 || to.getYear() > 9999) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ANALYSIS_PERIOD",
                "분석 기간은 1000년부터 9999년 사이이며 시작일이 종료일보다 늦을 수 없습니다.");
        }
        UserSettingsRecord user = userMapper.findCurrentUser(userId);
        if (user == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자 정보를 찾을 수 없습니다.");
        }
        if (!user.isConsentCompleted()) {
            return empty(AnalysisStatus.CONSENT_REQUIRED, from, to);
        }
        if (!user.isUsageLogAgreed()) {
            return empty(AnalysisStatus.CONSENT_DECLINED, from, to);
        }

        // DB의 초 단위 DATETIME과 같은 기준으로 종료일 전체를 포함한다.
        var startedFrom = from.atStartOfDay();
        var startedTo = to.atTime(23, 59, 59);
        var patterns = analysisMapper.findPatternUsage(userId, startedFrom, startedTo).stream()
            .map(row -> new PatternUsageResponse(row.getPatternId(),
                PatternType.valueOf(row.getPatternType()), row.getTitle(), row.getCompletedCount()))
            .toList();
        if (patterns.isEmpty()) {
            return empty(AnalysisStatus.NO_DATA, from, to);
        }
        var steps = analysisMapper.findStepAnalysis(userId, startedFrom, startedTo).stream()
            .map(this::toStep).toList();
        var difficultStep = steps.stream().min(DIFFICULTY_ORDER).orElse(null);
        return new UsageAnalysisResponse(AnalysisStatus.AVAILABLE, from, to, patterns, steps, difficultStep);
    }

    private StepAnalysisResponse toStep(StepAnalysisRecord row) {
        return new StepAnalysisResponse(row.getPatternId(), row.getStepId(), row.getStepCode(),
            row.getStepName(), row.getStepOrder(), row.getVisitCount(), row.getErrorScore(),
            row.getAverageDurationSeconds());
    }

    private UsageAnalysisResponse empty(AnalysisStatus status, LocalDate from, LocalDate to) {
        return new UsageAnalysisResponse(status, from, to, List.of(), List.of(), null);
    }
}
