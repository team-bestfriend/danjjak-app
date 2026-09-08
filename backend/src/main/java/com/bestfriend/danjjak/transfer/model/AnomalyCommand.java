package com.bestfriend.danjjak.transfer.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnomalyCommand {

    private Long anomalyEventId;
    private long userId;
    private Long patternExecutionId;
    private long sourceAccountId;
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
    private LocalDateTime detectedAt;
}
