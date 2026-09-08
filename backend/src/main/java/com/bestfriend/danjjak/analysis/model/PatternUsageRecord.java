package com.bestfriend.danjjak.analysis.model;

public class PatternUsageRecord {

    private long patternId;
    private String patternType;
    private String title;
    private long completedCount;

    public long getPatternId() { return patternId; }
    public void setPatternId(long patternId) { this.patternId = patternId; }
    public String getPatternType() { return patternType; }
    public void setPatternType(String patternType) { this.patternType = patternType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public long getCompletedCount() { return completedCount; }
    public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }
}
