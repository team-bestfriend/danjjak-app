package com.bestfriend.danjjak.account.model;

/**
 * 선택한 모의 본인 계좌 후보를 기존 자료 보존과 함께 추가하는 명령.
 * 협력: AccountService, AccountMapper, AccountRecord.
 * 근거: FR-058, SC-016. <a href="../../../../../../../../../docs/specs/requirements/auth-settings.md">상세 명세</a>.
 */
public class OwnedAccountImportCommand {

    private long userId;
    private long accountId;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }
}
