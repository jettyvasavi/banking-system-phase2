package com.bankingsystem.controller;


import com.bankingsystem.model.Account;
import com.bankingsystem.model.dto.BalanceRequest;
import com.bankingsystem.model.dto.CreateAccountRequest;
import com.bankingsystem.model.dto.StatusRequest;
import com.bankingsystem.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account newAccount = accountService.createAccount(request);
        return new ResponseEntity<>(newAccount, HttpStatus.CREATED);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        Account account = accountService.getAccount(accountNumber);
        return ResponseEntity.ok(account);
    }

    @PutMapping("/{accountNumber}/balance")
    public ResponseEntity<Account> updateBalance(@PathVariable String accountNumber,
                                                 @Valid @RequestBody BalanceRequest request) {
        Account updatedAccount = accountService.updateBalance(accountNumber, request.getAmount());
        return ResponseEntity.ok(updatedAccount);
    }

    @PutMapping("/{accountNumber}/status")
    public ResponseEntity<Account> updateStatus(@PathVariable String accountNumber,
                                                @Valid @RequestBody StatusRequest request) {
        Account updatedAccount = accountService.updateStatus(accountNumber, request.getStatus());
        return ResponseEntity.ok(updatedAccount);
    }
}
