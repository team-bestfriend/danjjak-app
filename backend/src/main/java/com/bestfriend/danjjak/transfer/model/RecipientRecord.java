package com.bestfriend.danjjak.transfer.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipientRecord {

    private Long accountId;
    private String name;
    private String bankCode;
    private String bankName;
    private String accountNumber;
}
