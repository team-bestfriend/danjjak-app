package com.bestfriend.danjjak.transfer.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionCommand {

    private Long transactionId;
    private long userId;
    private long sourceAccountId;
    private Long recipientAccountId;
    private Long patternExecutionId;
    private BigDecimal amount;
    private String recipientName;
    private String recipientBankCode;
    private String recipientBankName;
    private String recipientAccountNumber;
    private String description;
    private BigDecimal balanceAfter;
    private LocalDateTime transactionAt;
}
