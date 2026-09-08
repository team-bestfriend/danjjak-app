package com.bestfriend.danjjak.account.service;

import com.bestfriend.danjjak.account.dto.AccountDtos.AccountImportRequest;
import com.bestfriend.danjjak.account.dto.AccountDtos.BalanceResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.DefaultAccountRequest;
import com.bestfriend.danjjak.account.dto.AccountDtos.ImportCandidateResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.OwnedAccountResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.RecipientAccountResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.RegisteredPersonRequest;
import com.bestfriend.danjjak.account.dto.AccountDtos.RegisteredPersonResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.RegisteredPersonUpdateRequest;
import com.bestfriend.danjjak.account.dto.AccountDtos.TransactionResponse;
import com.bestfriend.danjjak.account.mapper.AccountMapper;
import com.bestfriend.danjjak.account.model.AccountRecord;
import com.bestfriend.danjjak.account.model.OwnedAccountImportCommand;
import com.bestfriend.danjjak.account.model.RecipientAccountCommand;
import com.bestfriend.danjjak.account.model.RegisteredPersonCommand;
import com.bestfriend.danjjak.account.model.RegisteredPersonRecord;
import com.bestfriend.danjjak.common.error.ApiException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
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
        requireConsent(accountMapper.findAccountConsent(userId));
        return ownedResponses(userId);
    }

    @Transactional(readOnly = true)
    public List<ImportCandidateResponse> getImportCandidates(long userId) {
        requireConsent(accountMapper.findAccountConsent(userId));
        return accountMapper.findOwnedAccountCandidates(userId).stream()
                .sorted(Comparator.comparing(AccountRecord::getAccountId))
                .map(account -> new ImportCandidateResponse(
                        account.getAccountId().toString(), account.getBankCode(),
                        account.getBankName(), maskAccountNumber(account.getAccountNumber()),
                        account.getAccountAlias(), true, null))
                .toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<OwnedAccountResponse> importOwnedAccounts(long userId, AccountImportRequest request) {
        requireConsent(accountMapper.lockUserForAccountUpdate(userId));
        if (request == null || request.accountIds() == null || request.accountIds().isEmpty()) {
            throw invalidRequest();
        }
        List<Long> selected = new ArrayList<>();
        Set<Long> unique = new HashSet<>();
        for (String value : request.accountIds()) {
            long id = parseAccountId(value);
            if (!unique.add(id)) {
                throw invalidRequest();
            }
            selected.add(id);
        }

        List<AccountRecord> imported = accountMapper.findOwnedAccounts(userId);
        Set<Long> importedIds = new HashSet<>();
        imported.forEach(account -> importedIds.add(account.getAccountId()));
        Set<Long> allowedIds = new HashSet<>(importedIds);
        accountMapper.findOwnedAccountCandidates(userId)
                .forEach(account -> allowedIds.add(account.getAccountId()));
        if (!allowedIds.containsAll(selected)) {
            throw invalidAccountSelection();
        }

        // 후보에 남아 있는 기본 표시가 추가 불러오기로 기존 기본값을 바꾸지 않게 합니다.
        accountMapper.clearUnimportedAccountDefaults(userId);
        for (long accountId : selected) {
            if (!importedIds.contains(accountId)) {
                OwnedAccountImportCommand command = new OwnedAccountImportCommand();
                command.setUserId(userId);
                command.setAccountId(accountId);
                if (accountMapper.importOwnedAccount(command) != 1) {
                    throw invalidAccountSelection();
                }
            }
        }
        if (imported.isEmpty()) {
            if (accountMapper.setOwnedAccountDefault(userId, selected.get(0)) != 1) {
                throw invalidAccountSelection();
            }
        }
        return ownedResponses(userId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OwnedAccountResponse setDefaultAccount(long userId, DefaultAccountRequest request) {
        requireConsent(accountMapper.lockUserForAccountUpdate(userId));
        long accountId = parseAccountId(request == null ? null : request.accountId());
        if (accountMapper.findOwnedAccount(userId, accountId) == null) {
            throw invalidAccountSelection();
        }
        accountMapper.clearOwnedAccountDefaults(userId);
        if (accountMapper.setOwnedAccountDefault(userId, accountId) != 1) {
            throw invalidAccountSelection();
        }
        return toOwnedResponse(accountMapper.findOwnedAccount(userId, accountId));
    }

    private List<OwnedAccountResponse> ownedResponses(long userId) {
        return accountMapper.findOwnedAccounts(userId).stream()
                .sorted(Comparator.comparing(AccountRecord::getAccountId))
                .map(this::toOwnedResponse).toList();
    }

    private void requireConsent(Boolean completed) {
        if (completed == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "SESSION_REQUIRED", "로그인이 필요합니다.");
        }
        if (!completed) {
            throw new ApiException(HttpStatus.FORBIDDEN, "CONSENT_NOT_COMPLETED",
                    "선택 동의를 저장한 뒤 다시 시도해 주세요.");
        }
    }

    private long parseAccountId(String value) {
        if (value == null || !value.matches("^[1-9][0-9]{0,18}$")) {
            throw invalidRequest();
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw invalidRequest();
        }
    }

    private ApiException invalidRequest() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "계좌 선택을 확인해 주세요.");
    }

    private ApiException invalidAccountSelection() {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_REQUEST",
                "선택할 수 없는 계좌가 포함되어 있어요. 계좌 목록을 다시 확인해 주세요.");
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(long userId, long accountId) {
        requireConsent(accountMapper.findAccountConsent(userId));
        AccountRecord account = requireOwnedAccount(userId, accountId);
        return new BalanceResponse(account.getAccountId().toString(), account.getBalance());
    }

    @Transactional(readOnly = true)
    public List<RegisteredPersonResponse> getRegisteredPersons(long userId) {
        requireConsent(accountMapper.findAccountConsent(userId));
        return accountMapper.findRegisteredPersons(userId).stream()
                .map(person -> toRegisteredResponse(userId, person))
                .toList();
    }

    @Transactional
    public RegisteredPersonResponse createRegisteredPerson(
            long userId, RegisteredPersonRequest request) {
        requireConsent(accountMapper.lockUserForAccountUpdate(userId));
        RegisteredPersonCommand command = toCommand(userId, null, request);
        try {
            accountMapper.insertRegisteredPerson(command);
            RecipientAccountCommand account = new RecipientAccountCommand();
            account.setUserId(userId);
            account.setRegisteredPersonId(command.getRegisteredPersonId());
            account.setBankCode(command.getBankCode());
            account.setBankName(command.getBankName());
            account.setAccountNumber(command.getAccountNumber());
            account.setAccountAlias(command.getAccountAlias());
            accountMapper.insertRecipientAccount(account);
        } catch (DuplicateKeyException exception) {
            throw new ApiException(
                    HttpStatus.CONFLICT, "ACCOUNT_ALREADY_EXISTS", "이미 등록된 계좌입니다.");
        }
        return toRegisteredResponse(
                userId, accountMapper.findRegisteredPerson(userId, command.getRegisteredPersonId()));
    }

    @Transactional
    public RegisteredPersonResponse updateRegisteredPerson(
            long userId, long registeredPersonId, RegisteredPersonUpdateRequest request) {
        requireConsent(accountMapper.lockUserForAccountUpdate(userId));
        RegisteredPersonRecord current =
                accountMapper.findRegisteredPerson(userId, registeredPersonId);
        if (current == null) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND, "REGISTERED_PERSON_NOT_FOUND", "등록 인물을 찾을 수 없습니다.");
        }

        RegisteredPersonCommand command = new RegisteredPersonCommand();
        command.setUserId(userId);
        command.setRegisteredPersonId(registeredPersonId);
        command.setName(request.name().trim());
        command.setRelationship(request.relationship().trim());
        accountMapper.updateRegisteredPerson(command);
        return toRegisteredResponse(userId, accountMapper.findRegisteredPerson(userId, registeredPersonId));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(
            long userId, long accountId, String category) {
        requireConsent(accountMapper.findAccountConsent(userId));
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
                    HttpStatus.NOT_FOUND, "SOURCE_ACCOUNT_NOT_FOUND", "본인 계좌를 찾을 수 없습니다.");
        }
        return account;
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        String normalized = category.trim().toUpperCase(Locale.ROOT);
        if (!CATEGORIES.contains(normalized)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "지원하지 않는 거래 카테고리입니다.");
        }
        return normalized;
    }

    private OwnedAccountResponse toOwnedResponse(AccountRecord account) {
        return new OwnedAccountResponse(
                account.getAccountId().toString(),
                account.getBankCode(),
                account.getBankName(),
                maskAccountNumber(account.getAccountNumber()),
                account.getAccountAlias(),
                account.getBalance(),
                account.isPrimary());
    }

    private RegisteredPersonResponse toRegisteredResponse(long userId, RegisteredPersonRecord record) {
        return new RegisteredPersonResponse(
                record.getRegisteredPersonId().toString(),
                record.getName(),
                record.getRelationship(),
                accountMapper.findRegisteredPersonAccounts(userId, record.getRegisteredPersonId())
                        .stream().map(account -> new RecipientAccountResponse(
                                account.getAccountId().toString(), account.getBankCode(), account.getBankName(),
                                maskAccountNumber(account.getAccountNumber()), account.getAccountAlias()))
                        .toList());
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            return "****";
        }
        String digits = accountNumber.replaceAll("[^0-9]", "");
        int visible = Math.min(4, Math.max(0, digits.length() - 4));
        return "*".repeat(digits.length() - visible) + digits.substring(digits.length() - visible);
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
