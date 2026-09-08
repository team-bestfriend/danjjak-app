package com.bestfriend.danjjak.analysis.mapper;

import com.bestfriend.danjjak.analysis.model.PatternUsageRecord;
import com.bestfriend.danjjak.analysis.model.StepAnalysisRecord;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UsageAnalysisMapper {

    List<PatternUsageRecord> findPatternUsage(
            @Param("userId") long userId,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<StepAnalysisRecord> findStepAnalysis(
            @Param("userId") long userId,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
