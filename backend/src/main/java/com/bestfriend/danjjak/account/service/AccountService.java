package com.bestfriend.danjjak.account.service;

import com.bestfriend.danjjak.account.dto.AccountDtos.BalanceResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.OwnedAccountResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.RecipientAccountResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.RegisteredPersonRequest;
import com.bestfriend.danjjak.account.dto.AccountDtos.RegisteredPersonResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.TransactionResponse;
import com.bestfriend.danjjak.account.mapper.AccountMapper;
import com.bestfriend.danjjak.account.model.AccountRecord;
import com.bestfriend.danjjak.account.model.RegisteredPersonAccountRecord;
import com.bestfriend.danjjak.account.model.RegisteredPersonCommand;
import com.bestfriend.danjjak.common.error.ApiException;
import java.util.List;
import java.util.Set;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private static final Set<String> CATEGORIES =
            Set.of(
                    "GENERAL",
                    "TRANSFER",
                    "PENSION",
                    "MANAGEMENT_FEE",
                    "UTILITY_BILL",
                    "AUTO_TRANSFER",
                    "CARD");

    private final AccountMapper accountMapper;

    public AccountService(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Transactional(readOnly = true)
    public List<OwnedAccountResponse> getOwnedAccounts(long userId) {
        return accountMapper.findOwnedAccounts(userId).stream().map(this::toOwnedResponse).toList();
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(long userId, long accountId) {
        AccountRecord account = requireOwnedAccount(userId, accountId);
        return new BalanceResponse(account.getAccountId(), account.getBalance());
    }

    @Transactional(readOnly = true)
    public List<RegisteredPersonResponse> getRegisteredPersons(long userId) {
        return accountMapper.findRegisteredPersons(userId).stream()
                .map(this::toRegisteredResponse)
                .toList();
    }

    @Transactional
    public RegisteredPersonResponse createRegisteredPerson(
            long userId, RegisteredPersonRequest request) {
        RegisteredPersonCommand command = toCommand(userId, null, request);
        try {
            accountMapper.insertRegisteredPerson(command);
            accountMapper.insertRecipientAccount(command);
        } catch (DuplicateKeyException exception) {
            throw new ApiException(
                    HttpStatus.CONFLICT, "ACCOUNT_ALREADY_EXISTS", "이미 등록된 계좌입니다.");
        }
        return toRegisteredResponse(
                accountMapper.findRegisteredPerson(userId, command.getRegisteredPersonId()));
    }

    @Transactional
    public RegisteredPersonResponse updateRegisteredPerson(
            long userId, long registeredPersonId, RegisteredPersonRequest request) {
        RegisteredPersonAccountRecord current =
                accountMapper.findRegisteredPerson(userId, registeredPersonId);
        if (current == null) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND, "REGISTERED_PERSON_NOT_FOUND", "등록 인물을 찾을 수 없습니다.");
        }

        RegisteredPersonCommand command = toCommand(userId, registeredPersonId, request);
        command.setAccountId(current.getAccountId());
        try {
            accountMapper.updateRegisteredPerson(command);
            accountMapper.updateRecipientAccount(command);
        } catch (DuplicateKeyException exception) {
            throw new ApiException(
                    HttpStatus.CONFLICT, "ACCOUNT_ALREADY_EXISTS", "이미 등록된 계좌입니다.");
        }
        return toRegisteredResponse(accountMapper.findRegisteredPerson(userId, registeredPersonId));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(
            long userId, long accountId, String category) {
        requireOwnedAccount(userId, accountId);
        String normalizedCategory = normalizeCategory(category);
        return accountMapper.findTransactions(userId, accountId, normalizedCategory).stream()
                .map(
                        transaction ->
                                new TransactionResponse(
                                        transaction.getTransactionId(),
                                        transaction.getTransactionType(),
                                        transaction.getCategory(),
                                        transaction.getAmount(),
                                        transaction.getCounterpartyName(),
                                        transaction.getDescription(),
                                        transaction.getBalanceAfter(),
                                        transaction.getTransactionAt()))
                .toList();
    }

    private AccountRecord requireOwnedAccount(long userId, long accountId) {
        AccountRecord account = accountMapper.findOwnedAccount(userId, accountId);
        if (account == null) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "본인 계좌를 찾을 수 없습니다.");
        }
        return account;
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        String normalized = category.trim().toUpperCase();
        if (!CATEGORIES.contains(normalized)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, "INVALID_CATEGORY", "지원하지 않는 거래 카테고리입니다.");
        }
        return normalized;
    }

    private OwnedAccountResponse toOwnedResponse(AccountRecord account) {
        return new OwnedAccountResponse(
                account.getAccountId(),
                account.getBankCode(),
                account.getBankName(),
                account.getAccountNumber(),
                account.getAccountAlias(),
                account.getBalance(),
                account.isPrimary());
    }

    private RegisteredPersonResponse toRegisteredResponse(RegisteredPersonAccountRecord record) {
        return new RegisteredPersonResponse(
                record.getRegisteredPersonId(),
                record.getName(),
                record.getRelationship(),
                new RecipientAccountResponse(
                        record.getAccountId(),
                        record.getBankCode(),
                        record.getBankName(),
                        record.getAccountNumber(),
                        record.getAccountAlias()));
    }

    private RegisteredPersonCommand toCommand(
            long userId, Long registeredPersonId, RegisteredPersonRequest request) {
        RegisteredPersonCommand command = new RegisteredPersonCommand();
        command.setUserId(userId);
        command.setRegisteredPersonId(registeredPersonId);
        command.setName(request.name().trim());
        command.setRelationship(request.relationship().trim());
        command.setBankCode(request.bankCode().trim());
        command.setBankName(request.bankName().trim());
        command.setAccountNumber(request.accountNumber().trim());
        command.setAccountAlias(
                request.accountAlias() == null || request.accountAlias().isBlank()
                        ? null
                        : request.accountAlias().trim());
        return command;
    }
}
