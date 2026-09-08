package com.bestfriend.danjjak.user.model;

public class UserSettingsRecord {

    private long userId;
    private Long kakaoUserId;
    private String name;
    private boolean usageLogAgreed;
    private boolean guardianShareAgreed;
    private boolean consentCompleted;
    private String fontSize;
    private String voiceSpeed;
    private String guideVoiceType;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Long getKakaoUserId() {
        return kakaoUserId;
    }

    public void setKakaoUserId(Long kakaoUserId) {
        this.kakaoUserId = kakaoUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUsageLogAgreed() {
        return usageLogAgreed;
    }

    public void setUsageLogAgreed(boolean usageLogAgreed) {
        this.usageLogAgreed = usageLogAgreed;
    }

    public boolean isGuardianShareAgreed() {
        return guardianShareAgreed;
    }

    public void setGuardianShareAgreed(boolean guardianShareAgreed) {
        this.guardianShareAgreed = guardianShareAgreed;
    }

    public boolean isConsentCompleted() {
        return consentCompleted;
    }

    public void setConsentCompleted(boolean consentCompleted) {
        this.consentCompleted = consentCompleted;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public String getVoiceSpeed() {
        return voiceSpeed;
    }

    public void setVoiceSpeed(String voiceSpeed) {
        this.voiceSpeed = voiceSpeed;
    }

    public String getGuideVoiceType() {
        return guideVoiceType;
    }

    public void setGuideVoiceType(String guideVoiceType) {
        this.guideVoiceType = guideVoiceType;
    }
}
