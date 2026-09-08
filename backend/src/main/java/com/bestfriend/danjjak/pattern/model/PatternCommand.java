package com.bestfriend.danjjak.pattern.model;

public class PatternCommand {

    private long patternId;
    private long userId;
    private int shortcutNumber;
    private String patternType;
    private String title;
    private String description;
    private Long linkedAccountId;

    public long getPatternId() { return patternId; }
    public void setPatternId(long patternId) { this.patternId = patternId; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public int getShortcutNumber() { return shortcutNumber; }
    public void setShortcutNumber(int shortcutNumber) { this.shortcutNumber = shortcutNumber; }
    public String getPatternType() { return patternType; }
    public void setPatternType(String patternType) { this.patternType = patternType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getLinkedAccountId() { return linkedAccountId; }
    public void setLinkedAccountId(Long linkedAccountId) { this.linkedAccountId = linkedAccountId; }
}
