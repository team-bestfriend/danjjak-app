package com.bestfriend.danjjak.account.model;

/**
 * 기존 사람의 특정 계좌 추가·수정 의도를 독립적으로 전달하는 저장 명령.
 * 협력: AccountService, AccountMapper, RegisteredPersonRecord.
 * 근거: FR-006, FR-014, SC-017. <a href="../../../../../../../../../docs/specs/requirements/people-accounts.md">상세 명세</a>.
 */
public class RecipientAccountCommand {

    private long userId;
    private long registeredPersonId;
    private Long accountId;
    private String bankCode;
    private String bankName;
    private String accountNumber;
    private String accountAlias;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getRegisteredPersonId() {
        return registeredPersonId;
    }

    public void setRegisteredPersonId(long registeredPersonId) {
        this.registeredPersonId = registeredPersonId;
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
