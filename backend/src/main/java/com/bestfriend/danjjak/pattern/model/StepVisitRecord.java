package com.bestfriend.danjjak.pattern.model;

import java.time.LocalDateTime;

public class StepVisitRecord {

    private long visitId;
    private long executionId;
    private long stepId;
    private int visitNumber;
    private int retryCount;
    private int backCount;
    private int wrongTouchCount;
    private boolean routeDeviation;
    private boolean completed;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public long getVisitId() { return visitId; }
    public void setVisitId(long visitId) { this.visitId = visitId; }
    public long getExecutionId() { return executionId; }
    public void setExecutionId(long executionId) { this.executionId = executionId; }
    public long getStepId() { return stepId; }
    public void setStepId(long stepId) { this.stepId = stepId; }
    public int getVisitNumber() { return visitNumber; }
    public void setVisitNumber(int visitNumber) { this.visitNumber = visitNumber; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public int getBackCount() { return backCount; }
    public void setBackCount(int backCount) { this.backCount = backCount; }
    public int getWrongTouchCount() { return wrongTouchCount; }
    public void setWrongTouchCount(int wrongTouchCount) { this.wrongTouchCount = wrongTouchCount; }
    public boolean isRouteDeviation() { return routeDeviation; }
    public void setRouteDeviation(boolean routeDeviation) { this.routeDeviation = routeDeviation; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
}
