package com.bankingsystem.service;

import com.bankingsystem.exception.AccountNotFoundException;
import com.bankingsystem.model.Account;
import com.bankingsystem.model.dto.BalanceRequest;
import com.bankingsystem.model.dto.CreateAccountRequest;
import com.bankingsystem.model.dto.StatusRequest;
import com.bankingsystem.repository.AccountRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    AccountRepository accountRepository;

    private final Random random = new Random();


    public Account createAccount(@Valid CreateAccountRequest request) {
        log.info("Creating account for holder: {}", request.getHolderName());
        String accountNumber = generateAccountNumber(request.getHolderName());
        Account account = new Account(accountNumber, request.getHolderName());
        account.setBalance(0.0);
        account.setStatus("ACTIVE");
        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully with number: {}", savedAccount.getAccountNumber());
        return savedAccount;
    }

    public Account getAccount(String accountNumber) {
        log.info("Fetching account: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.warn("Account not found: {}", accountNumber);
                    return new AccountNotFoundException("Account not found with number: " + accountNumber);
                });
    }

    public Account updateBalance(String accountNumber, Double amount) {
        log.info("Updating balance for account: {} by amount: {}", accountNumber, amount);

        Account account = getAccount(accountNumber);

        if ("INACTIVE".equalsIgnoreCase(account.getStatus())) {
            log.error("Transaction failed. Account {} is inactive.", accountNumber);
            throw new RuntimeException("Account is INACTIVE. Cannot process transaction.");
        }

        account.setBalance(account.getBalance() + amount);
        Account savedAccount = accountRepository.save(account);

        log.info("New Balance for {}: {}", accountNumber, savedAccount.getBalance());
        return savedAccount;
    }

    public Account updateStatus(String accountNumber, String status) {
        log.info("Updating status for account: {} to {}", accountNumber, status);

        Account account = getAccount(accountNumber);

        if (!status.equalsIgnoreCase("ACTIVE") && !status.equalsIgnoreCase("INACTIVE")) {
            throw new RuntimeException("Invalid status. Use ACTIVE or INACTIVE.");
        }

        account.setStatus(status.toUpperCase());
        return accountRepository.save(account);
    }

    private String generateAccountNumber(String name) {
        String initials = Arrays.stream(name.split("\\s+"))
                .filter(s -> !s.isEmpty())
                .map(s -> String.valueOf(s.charAt(0)))
                .collect(Collectors.joining())
                .toUpperCase();
        String uniqueAccountNumber;
        do {
            int randomNumber = 1000 + random.nextInt(9000);
            uniqueAccountNumber = initials + randomNumber;
        } while (accountRepository.existsByAccountNumber(uniqueAccountNumber));
        return uniqueAccountNumber;
    }
}
