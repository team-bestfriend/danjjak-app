package com.bestfriend.danjjak.account.model;

public class RegisteredPersonAccountRecord {

    private Long registeredPersonId;
    private String name;
    private String relationship;
    private Long accountId;
    private String bankCode;
    private String bankName;
    private String accountNumber;
    private String accountAlias;

    public Long getRegisteredPersonId() {
        return registeredPersonId;
    }

    public void setRegisteredPersonId(Long registeredPersonId) {
        this.registeredPersonId = registeredPersonId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountAlias() {
        return accountAlias;
    }

    public void setAccountAlias(String accountAlias) {
        this.accountAlias = accountAlias;
    }
}
