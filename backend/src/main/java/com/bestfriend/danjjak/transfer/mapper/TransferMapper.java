package com.bestfriend.danjjak.transfer.mapper;

import com.bestfriend.danjjak.transfer.model.AnomalyCommand;
import com.bestfriend.danjjak.transfer.model.AnomalyRecord;
import com.bestfriend.danjjak.transfer.model.RecipientRecord;
import com.bestfriend.danjjak.transfer.model.TransactionCommand;
import com.bestfriend.danjjak.transfer.model.TransferAccountRecord;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TransferMapper {

    TransferAccountRecord findSourceAccountForUpdate(
            @Param("userId") long userId, @Param("accountId") long accountId);

    RecipientRecord findRegisteredRecipient(
            @Param("userId") long userId, @Param("accountId") long accountId);

    int countAvailablePatternExecution(
            @Param("userId") long userId,
            @Param("patternExecutionId") long patternExecutionId,
            @Param("sourceAccountId") long sourceAccountId);

    int assignPatternExecutionSource(
            @Param("userId") long userId,
            @Param("patternExecutionId") long patternExecutionId,
            @Param("sourceAccountId") long sourceAccountId);

    int countRecentTransfers(
            @Param("userId") long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    int debitAccount(
            @Param("userId") long userId,
            @Param("accountId") long accountId,
            @Param("amount") BigDecimal amount);

    int insertTransaction(TransactionCommand command);

    int insertAnomaly(AnomalyCommand command);

    AnomalyRecord findPendingMatchingAnomalyForUpdate(AnomalyCommand command);

    AnomalyRecord findAnomalyForUpdate(
            @Param("userId") long userId, @Param("anomalyEventId") long anomalyEventId);

    int resolveAnomalyAsCancelled(
            @Param("userId") long userId,
            @Param("anomalyEventId") long anomalyEventId,
            @Param("rechecked") boolean rechecked,
            @Param("resolvedAt") LocalDateTime resolvedAt);

    int resolveAnomalyAsContinued(
            @Param("userId") long userId,
            @Param("anomalyEventId") long anomalyEventId,
            @Param("transactionId") long transactionId,
            @Param("rechecked") boolean rechecked,
            @Param("resolvedAt") LocalDateTime resolvedAt);

    int finishPatternExecution(
            @Param("patternExecutionId") long patternExecutionId,
            @Param("status") String status,
            @Param("endedAt") LocalDateTime endedAt);
}
