package com.bestfriend.danjjak.transfer.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferAccountRecord {

    private Long accountId;
    private String pinHash;
    private BigDecimal balance;
}
