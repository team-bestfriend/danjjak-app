package com.bestfriend.danjjak.account.model;

/**
 * 계좌와 분리된 사람 식별·이름·관계를 표현하는 업무 모델.
 * 협력: AccountService, AccountMapper, RegisteredPersonAccountRecord.
 * 근거: FR-005, FR-006, SC-017. <a href="../../../../../../../../../docs/specs/requirements/people-accounts.md">상세 명세</a>.
 */
public class RegisteredPersonRecord {

    private Long registeredPersonId;
    private String name;
    private String relationship;

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
}
