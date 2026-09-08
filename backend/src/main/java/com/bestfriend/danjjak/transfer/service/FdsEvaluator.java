package com.bestfriend.danjjak.transfer.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FdsEvaluator {

    static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("10000000");
    static final int REPEATED_TRANSFER_THRESHOLD = 2;

    public FdsResult evaluate(BigDecimal amount, int recentTransferCount) {
        boolean highAmount = amount.compareTo(HIGH_AMOUNT_THRESHOLD) >= 0;
        boolean repeatedTransfer = recentTransferCount >= REPEATED_TRANSFER_THRESHOLD;

        List<String> reasons = new ArrayList<>();
        if (highAmount) {
            reasons.add("HIGH_AMOUNT");
        }
        if (repeatedTransfer) {
            reasons.add("REPEATED_TRANSFER");
        }

        String riskLevel =
                reasons.size() == 2 ? "HIGH" : reasons.size() == 1 ? "MEDIUM" : "NORMAL";
        return new FdsResult(
                riskLevel, List.copyOf(reasons), highAmount, repeatedTransfer, recentTransferCount);
    }

    public record FdsResult(
            String riskLevel,
            List<String> reasons,
            boolean highAmountDetected,
            boolean repeatedTransferDetected,
            int recentTransferCount) {

        public boolean anomalous() {
            return !"NORMAL".equals(riskLevel);
        }
    }
}
