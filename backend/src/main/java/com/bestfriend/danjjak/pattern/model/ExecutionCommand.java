package com.bestfriend.danjjak.pattern.model;

import java.time.LocalDateTime;

public class ExecutionCommand {

    private long executionId;
    private long patternId;
    private Long sourceAccountId;
    private LocalDateTime startedAt;

    public long getExecutionId() { return executionId; }
    public void setExecutionId(long executionId) { this.executionId = executionId; }
    public long getPatternId() { return patternId; }
    public void setPatternId(long patternId) { this.patternId = patternId; }
    public Long getSourceAccountId() { return sourceAccountId; }
    public void setSourceAccountId(Long sourceAccountId) { this.sourceAccountId = sourceAccountId; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
}
