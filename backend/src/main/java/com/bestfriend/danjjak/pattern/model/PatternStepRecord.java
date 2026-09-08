package com.bestfriend.danjjak.pattern.model;

public class PatternStepRecord {

    private long stepId;
    private long patternId;
    private int stepOrder;
    private String stepCode;
    private String stepName;
    private String instructionText;
    private String screenCode;
    private String targetElementId;
    private String voiceFilePath;
    private String voiceContentType;

    public long getStepId() { return stepId; }
    public void setStepId(long stepId) { this.stepId = stepId; }
    public long getPatternId() { return patternId; }
    public void setPatternId(long patternId) { this.patternId = patternId; }
    public int getStepOrder() { return stepOrder; }
    public void setStepOrder(int stepOrder) { this.stepOrder = stepOrder; }
    public String getStepCode() { return stepCode; }
    public void setStepCode(String stepCode) { this.stepCode = stepCode; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getInstructionText() { return instructionText; }
    public void setInstructionText(String instructionText) { this.instructionText = instructionText; }
    public String getScreenCode() { return screenCode; }
    public void setScreenCode(String screenCode) { this.screenCode = screenCode; }
    public String getTargetElementId() { return targetElementId; }
    public void setTargetElementId(String targetElementId) { this.targetElementId = targetElementId; }
    public String getVoiceFilePath() { return voiceFilePath; }
    public void setVoiceFilePath(String voiceFilePath) { this.voiceFilePath = voiceFilePath; }
    public String getVoiceContentType() { return voiceContentType; }
    public void setVoiceContentType(String voiceContentType) { this.voiceContentType = voiceContentType; }
}
