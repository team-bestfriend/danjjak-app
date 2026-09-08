package com.bestfriend.danjjak.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public final class AccountDtos {

    private AccountDtos() {}

    public record OwnedAccountResponse(
            long accountId,
            String bankCode,
            String bankName,
            String accountNumber,
            String accountAlias,
            BigDecimal balance,
            boolean primary) {}

    public record RecipientAccountResponse(
            long accountId,
            String bankCode,
            String bankName,
            String accountNumber,
            String accountAlias) {}

    public record RegisteredPersonResponse(
            long registeredPersonId,
            String name,
            String relationship,
            RecipientAccountResponse account) {}

    public record RegisteredPersonRequest(
            @NotBlank @Size(max = 50) String name,
            @NotBlank @Size(max = 30) String relationship,
            @NotBlank @Size(max = 20) String bankCode,
            @NotBlank @Size(max = 50) String bankName,
            @NotBlank
                    @Size(min = 8, max = 50)
                    @Pattern(regexp = "^[0-9-]+$")
                    String accountNumber,
            @Size(max = 50) String accountAlias) {}

    public record BalanceResponse(long accountId, BigDecimal balance) {}

    public record TransactionResponse(
            long transactionId,
            String transactionType,
            String category,
            BigDecimal amount,
            String counterpartyName,
            String description,
            BigDecimal balanceAfter,
            LocalDateTime transactionAt) {}
}
