package com.bestfriend.danjjak.support.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationAnomalyRecord {

    private Long anomalyEventId;
    private String recipientName;
    private BigDecimal amount;
    private String riskLevel;
    private String finalAction;
    private LocalDateTime guardianNotifiedAt;
}
