package com.bestfriend.danjjak.account.controller;

import com.bestfriend.danjjak.account.dto.AccountDtos.BalanceResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.OwnedAccountResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.RegisteredPersonRequest;
import com.bestfriend.danjjak.account.dto.AccountDtos.RegisteredPersonResponse;
import com.bestfriend.danjjak.account.dto.AccountDtos.TransactionResponse;
import com.bestfriend.danjjak.account.service.AccountService;
import com.bestfriend.danjjak.common.session.DemoSessionUserResolver;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/accounts")
    public List<OwnedAccountResponse> getAccounts(HttpSession session) {
        return accountService.getOwnedAccounts(userResolver.resolveUserId(session));
    }

    @GetMapping("/accounts/{accountId}/balance")
    public BalanceResponse getBalance(
            @PathVariable long accountId, HttpSession session) {
        return accountService.getBalance(userResolver.resolveUserId(session), accountId);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public List<TransactionResponse> getTransactions(
            @PathVariable long accountId,
            @RequestParam(required = false) String category,
            HttpSession session) {
        return accountService.getTransactions(
                userResolver.resolveUserId(session), accountId, category);
    }

    @GetMapping("/registered-persons")
    public List<RegisteredPersonResponse> getRegisteredPersons(HttpSession session) {
        return accountService.getRegisteredPersons(userResolver.resolveUserId(session));
    }

    @PostMapping("/registered-persons")
    public ResponseEntity<RegisteredPersonResponse> createRegisteredPerson(
            @Valid @RequestBody RegisteredPersonRequest request, HttpSession session) {
        RegisteredPersonResponse response =
                accountService.createRegisteredPerson(
                        userResolver.resolveUserId(session), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/registered-persons/{registeredPersonId}")
    public RegisteredPersonResponse updateRegisteredPerson(
            @PathVariable long registeredPersonId,
            @Valid @RequestBody RegisteredPersonRequest request,
            HttpSession session) {
        return accountService.updateRegisteredPerson(
                userResolver.resolveUserId(session), registeredPersonId, request);
    }
}
