package com.bankingsystem.controller;
import com.bankingsystem.dto.TransactionRequest;
import com.bankingsystem.dto.TransferRequest;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(@Valid @RequestBody TransactionRequest request) {
        log.info("Received deposit request for account: {}", request.getAccountNumber());
        Transaction transaction = transactionService.deposit(request);
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdraw(@Valid @RequestBody TransactionRequest request) {
        log.info("Received withdraw request for account: {}", request.getAccountNumber());
        Transaction transaction = transactionService.withdraw(request);
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("Received transfer request from {} to {}", request.getSourceAccount(), request.getDestinationAccount());
        String response = transactionService.transfer(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable String accountNumber) {
        log.info("Fetching transaction history for account: {}", accountNumber);
        List<Transaction> transactions = transactionService.getTransactions(accountNumber);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }
}