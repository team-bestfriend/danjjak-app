package com.bestfriend.danjjak.analysis.model;

import java.math.BigDecimal;

public class StepAnalysisRecord {

    private long patternId;
    private long stepId;
    private String stepCode;
    private String stepName;
    private int stepOrder;
    private long visitCount;
    private long errorScore;
    private BigDecimal averageDurationSeconds;

    public long getPatternId() { return patternId; }
    public void setPatternId(long patternId) { this.patternId = patternId; }
    public long getStepId() { return stepId; }
    public void setStepId(long stepId) { this.stepId = stepId; }
    public String getStepCode() { return stepCode; }
    public void setStepCode(String stepCode) { this.stepCode = stepCode; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public int getStepOrder() { return stepOrder; }
    public void setStepOrder(int stepOrder) { this.stepOrder = stepOrder; }
    public long getVisitCount() { return visitCount; }
    public void setVisitCount(long visitCount) { this.visitCount = visitCount; }
    public long getErrorScore() { return errorScore; }
    public void setErrorScore(long errorScore) { this.errorScore = errorScore; }
    public BigDecimal getAverageDurationSeconds() { return averageDurationSeconds; }
    public void setAverageDurationSeconds(BigDecimal averageDurationSeconds) { this.averageDurationSeconds = averageDurationSeconds; }
}
