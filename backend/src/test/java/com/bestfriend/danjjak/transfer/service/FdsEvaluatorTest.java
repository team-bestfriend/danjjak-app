package com.bestfriend.danjjak.transfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FdsEvaluatorTest {

    private final FdsEvaluator evaluator = new FdsEvaluator();

    @ParameterizedTest
    @CsvSource({
        "9999999, 0, NORMAL, false, false",
        "9999999, 1, NORMAL, false, false",
        "10000000, 0, MEDIUM, true, false",
        "10000000, 1, MEDIUM, true, false",
        "1000, 2, MEDIUM, false, true",
        "1000, 3, MEDIUM, false, true",
        "10000000, 2, HIGH, true, true",
        "10000001, 3, HIGH, true, true"
    })
    void evaluatesOnlyAmountAndCompletedTransferCount(
            BigDecimal amount,
            int recentTransferCount,
            String riskLevel,
            boolean highAmount,
            boolean repeatedTransfer) {
        var result = evaluator.evaluate(amount, recentTransferCount);

        List<String> reasons = switch (riskLevel) {
            case "NORMAL" -> List.of();
            case "HIGH" -> List.of("HIGH_AMOUNT", "REPEATED_TRANSFER");
            default -> highAmount ? List.of("HIGH_AMOUNT") : List.of("REPEATED_TRANSFER");
        };

        assertEquals(riskLevel, result.riskLevel());
        assertEquals(reasons, result.reasons());
        assertEquals(highAmount, result.highAmountDetected());
        assertEquals(repeatedTransfer, result.repeatedTransferDetected());
        assertEquals(recentTransferCount, result.recentTransferCount());
        assertEquals(!reasons.isEmpty(), result.anomalous());
        assertThrows(UnsupportedOperationException.class, () -> result.reasons().add("OTHER"));
    }
}
