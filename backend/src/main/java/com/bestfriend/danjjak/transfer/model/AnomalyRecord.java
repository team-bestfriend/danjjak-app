package com.bestfriend.danjjak.transfer.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnomalyRecord {

    private Long anomalyEventId;
    private Long patternExecutionId;
    private Long sourceAccountId;
    private Long recipientAccountId;
    private String recipientName;
    private String recipientBankCode;
    private String recipientBankName;
    private String recipientAccountNumber;
    private BigDecimal amount;
    private boolean highAmountDetected;
    private boolean repeatedTransferDetected;
    private int recentTransferCount;
    private String riskLevel;
    private String finalAction;
    private Long transactionId;
    private BigDecimal balanceAfter;
}
