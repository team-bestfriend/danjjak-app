package com.bestfriend.danjjak.pattern.mapper;

import com.bestfriend.danjjak.pattern.model.ExecutionCommand;
import com.bestfriend.danjjak.pattern.model.PatternCommand;
import com.bestfriend.danjjak.pattern.model.PatternRecord;
import com.bestfriend.danjjak.pattern.model.PatternStepRecord;
import com.bestfriend.danjjak.pattern.model.StepCommand;
import com.bestfriend.danjjak.pattern.model.StepVisitRecord;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PatternMapper {

    List<PatternRecord> findActivePatterns(long userId);

    PatternRecord findActivePattern(
            @Param("userId") long userId, @Param("patternId") long patternId);

    List<PatternStepRecord> findPatternSteps(long patternId);

    List<Long> findActivePatternIdsForUpdate(long userId);

    int countShortcut(
            @Param("userId") long userId, @Param("shortcutNumber") int shortcutNumber);

    int countUserAccount(
            @Param("userId") long userId, @Param("accountId") long accountId);

    int countRegisteredRecipientAccount(
            @Param("userId") long userId, @Param("accountId") long accountId);

    Long findPrimaryAccountId(long userId);

    int insertPattern(PatternCommand command);

    int insertPatternStep(StepCommand command);

    int updatePattern(PatternCommand command);

    int updateStepInstruction(
            @Param("userId") long userId,
            @Param("patternId") long patternId,
            @Param("stepCode") String stepCode,
            @Param("instructionText") String instructionText);

    int deactivatePattern(
            @Param("userId") long userId, @Param("patternId") long patternId);

    int clearActiveShortcuts(long userId);

    int activatePatternAtShortcut(
            @Param("userId") long userId,
            @Param("patternId") long patternId,
            @Param("shortcutNumber") int shortcutNumber);

    boolean isUsageLogAgreed(long userId);

    int insertExecution(ExecutionCommand command);

    int countOwnedSourceAccount(
            @Param("userId") long userId, @Param("accountId") long accountId);

    String findExecutionStatusForUpdate(
            @Param("userId") long userId, @Param("executionId") long executionId);

    int countExecutionStep(
            @Param("userId") long userId,
            @Param("executionId") long executionId,
            @Param("stepId") long stepId);

    Integer nextVisitNumber(
            @Param("executionId") long executionId, @Param("stepId") long stepId);

    int insertStepVisit(StepVisitRecord visit);

    StepVisitRecord findStepVisitForUpdate(
            @Param("userId") long userId,
            @Param("executionId") long executionId,
            @Param("visitId") long visitId);

    StepVisitRecord findStepVisit(
            @Param("userId") long userId,
            @Param("executionId") long executionId,
            @Param("visitId") long visitId);

    int updateStepVisit(
            @Param("visitId") long visitId,
            @Param("retryCount") int retryCount,
            @Param("backCount") int backCount,
            @Param("wrongTouchCount") int wrongTouchCount,
            @Param("routeDeviation") boolean routeDeviation,
            @Param("completed") boolean completed,
            @Param("endedAt") LocalDateTime endedAt);

    LocalDateTime findExecutionEndedAt(
            @Param("userId") long userId, @Param("executionId") long executionId);

    int closeOpenVisits(
            @Param("executionId") long executionId,
            @Param("endedAt") LocalDateTime endedAt);

    int finishExecution(
            @Param("userId") long userId,
            @Param("executionId") long executionId,
            @Param("status") String status,
            @Param("endedAt") LocalDateTime endedAt);
}
