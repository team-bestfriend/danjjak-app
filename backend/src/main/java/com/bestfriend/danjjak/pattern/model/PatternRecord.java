package com.bestfriend.danjjak.pattern.model;

public class PatternRecord {

    private long patternId;
    private long userId;
    private int shortcutNumber;
    private String patternType;
    private String title;
    private String description;
    private Long linkedAccountId;
    private String bankCode;
    private String bankName;
    private String accountNumber;
    private String accountAlias;
    private Long registeredPersonId;
    private String registeredPersonName;
    private String relationship;

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
    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAccountAlias() { return accountAlias; }
    public void setAccountAlias(String accountAlias) { this.accountAlias = accountAlias; }
    public Long getRegisteredPersonId() { return registeredPersonId; }
    public void setRegisteredPersonId(Long registeredPersonId) { this.registeredPersonId = registeredPersonId; }
    public String getRegisteredPersonName() { return registeredPersonName; }
    public void setRegisteredPersonName(String registeredPersonName) { this.registeredPersonName = registeredPersonName; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
}
