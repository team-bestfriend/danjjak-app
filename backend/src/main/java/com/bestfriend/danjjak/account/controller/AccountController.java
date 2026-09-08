package com.bestfriend.danjjak.account.controller;

import com.bestfriend.danjjak.account.dto.AccountDtos.AccountImportRequest;
import com.bestfriend.danjjak.account.dto.AccountDtos.BalanceResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.DefaultAccountRequest;
import com.bestfriend.danjjak.account.dto.AccountDtos.ImportCandidateResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.OwnedAccountResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.RegisteredPersonRequest;
import com.bestfriend.danjjak.account.dto.AccountDtos.RegisteredPersonResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.RegisteredPersonUpdateRequest;
import com.bestfriend.danjjak.account.dto.AccountDtos.TransactionResponse;
import com.bestfriend.danjjak.account.service.AccountService;
import com.bestfriend.danjjak.common.session.DemoSessionUserResolver;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final AccountService accountService;
    private final DemoSessionUserResolver userResolver;

    public AccountController(AccountService accountService, DemoSessionUserResolver userResolver) {
        this.accountService = accountService;
        this.userResolver = userResolver;
    }

    @ModelAttribute
    public void disableResponseCaching(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
    }

    @GetMapping("/accounts/import-candidates")
    public List<ImportCandidateResponse> getImportCandidates(HttpServletRequest servletRequest) {
        return accountService.getImportCandidates(
                userResolver.resolveUserId(servletRequest.getSession(false)));
    }

    @PostMapping("/accounts/import")
    public List<OwnedAccountResponse> importOwnedAccounts(
            @Valid @RequestBody AccountImportRequest request,
            @RequestHeader(value = "X-CSRF-Token", required = false) String csrfToken,
            HttpServletRequest servletRequest) {
        long userId = userResolver.resolveUserId(servletRequest.getSession(false));
        userResolver.validateCsrfToken(servletRequest.getSession(false), csrfToken);
        return accountService.importOwnedAccounts(userId, request);
    }

    @PutMapping("/accounts/default")
    public OwnedAccountResponse setDefaultAccount(
            @Valid @RequestBody DefaultAccountRequest request,
            @RequestHeader(value = "X-CSRF-Token", required = false) String csrfToken,
            HttpServletRequest servletRequest) {
        long userId = userResolver.resolveUserId(servletRequest.getSession(false));
        userResolver.validateCsrfToken(servletRequest.getSession(false), csrfToken);
        return accountService.setDefaultAccount(userId, request);
    }

    @GetMapping("/accounts")
    public List<OwnedAccountResponse> getAccounts(HttpServletRequest servletRequest) {
        return accountService.getOwnedAccounts(userResolver.resolveUserId(servletRequest.getSession(false)));
    }

    @GetMapping("/accounts/{accountId}/balance")
    public BalanceResponse getBalance(
            @PathVariable long accountId, HttpServletRequest servletRequest) {
        return accountService.getBalance(userResolver.resolveUserId(servletRequest.getSession(false)), accountId);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public List<TransactionResponse> getTransactions(
            @PathVariable long accountId,
            @RequestParam(required = false) String category,
            HttpServletRequest servletRequest) {
        return accountService.getTransactions(
                userResolver.resolveUserId(servletRequest.getSession(false)), accountId, category);
    }

    @GetMapping("/registered-persons")
    public List<RegisteredPersonResponse> getRegisteredPersons(HttpServletRequest servletRequest) {
        return accountService.getRegisteredPersons(userResolver.resolveUserId(servletRequest.getSession(false)));
    }

    @PostMapping("/registered-persons")
    public ResponseEntity<RegisteredPersonResponse> createRegisteredPerson(
            @Valid @RequestBody RegisteredPersonRequest request,
            @RequestHeader(value = "X-CSRF-Token", required = false) String csrfToken,
            HttpServletRequest servletRequest) {
        userResolver.resolveUserId(servletRequest.getSession(false));
        userResolver.validateCsrfToken(servletRequest.getSession(false), csrfToken);
        RegisteredPersonResponse response =
                accountService.createRegisteredPerson(
                        userResolver.resolveUserId(servletRequest.getSession(false)), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/registered-persons/{registeredPersonId}")
    public RegisteredPersonResponse updateRegisteredPerson(
            @PathVariable long registeredPersonId,
            @Valid @RequestBody RegisteredPersonUpdateRequest request,
            @RequestHeader(value = "X-CSRF-Token", required = false) String csrfToken,
            HttpServletRequest servletRequest) {
        userResolver.resolveUserId(servletRequest.getSession(false));
        userResolver.validateCsrfToken(servletRequest.getSession(false), csrfToken);
        return accountService.updateRegisteredPerson(
                userResolver.resolveUserId(servletRequest.getSession(false)), registeredPersonId, request);
    }
}
