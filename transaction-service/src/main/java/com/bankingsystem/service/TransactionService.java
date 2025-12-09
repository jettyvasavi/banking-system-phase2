package com.bankingsystem.service;

import com.bankingsystem.client.AccountClient;
import com.bankingsystem.client.NotificationClient;
import com.bankingsystem.dto.AccountDTO;
import com.bankingsystem.dto.BalanceRequest;
import com.bankingsystem.dto.TransactionRequest;
import com.bankingsystem.dto.TransferRequest;
import com.bankingsystem.exception.InsufficientBalanceException;
import com.bankingsystem.exception.InvalidAccountException;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.repository.TransactionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final NotificationClient notificationClient;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountClient accountClient,
                              NotificationClient notificationClient) {
        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
    }

    @CircuitBreaker(name = "accountServiceCB", fallbackMethod = "fallbackTransaction")
    public Transaction deposit(TransactionRequest request) {
        log.info("Processing deposit of {} for account: {}", request.getAmount(), request.getAccountNumber());

        try {
            accountClient.updateBalance(request.getAccountNumber(), new BalanceRequest(request.getAmount()));

            Transaction txn = logTransaction("DEPOSIT", request.getAmount(), "SUCCESS", request.getAccountNumber());

            notificationClient.sendNotification("Deposit of " + request.getAmount() + " successful.");
            return txn;

        } catch (Exception e) {
            log.error("Deposit failed: {}", e.getMessage());
            logTransaction("DEPOSIT", request.getAmount(), "FAILED", request.getAccountNumber());
            throw e;
        }
    }

    @CircuitBreaker(name = "accountServiceCB", fallbackMethod = "fallbackTransaction")
    public Transaction withdraw(TransactionRequest request) {
        log.info("Processing withdrawal of {} for account: {}", request.getAmount(), request.getAccountNumber());

        try {
            AccountDTO account = accountClient.getAccount(request.getAccountNumber());
            if (account.getBalance() < request.getAmount()) {
                throw new InsufficientBalanceException("Insufficient funds. Available: " + account.getBalance());
            }

            accountClient.updateBalance(request.getAccountNumber(), new BalanceRequest(-request.getAmount()));
            Transaction txn = logTransaction("WITHDRAW", request.getAmount(), "SUCCESS", request.getAccountNumber());

            notificationClient.sendNotification("Withdrawal of " + request.getAmount() + " successful.");
            return txn;

        } catch (Exception e) {
            log.error("Withdrawal failed: {}", e.getMessage());
            logTransaction("WITHDRAW", request.getAmount(), "FAILED", request.getAccountNumber());
            throw e;
        }
    }

    @CircuitBreaker(name = "accountServiceCB", fallbackMethod = "fallbackTransfer")
    public String transfer(TransferRequest request) {
        log.info("Processing transfer from {} to {}", request.getSourceAccount(), request.getDestinationAccount());

        try {
            if (request.getSourceAccount().equals(request.getDestinationAccount())) {
                throw new InvalidAccountException("Source and Destination accounts cannot be the same.");
            }

            AccountDTO source = accountClient.getAccount(request.getSourceAccount());
            if (source.getBalance() < request.getAmount()) {
                throw new InsufficientBalanceException("Insufficient funds in source account.");
            }

            accountClient.getAccount(request.getDestinationAccount());

            accountClient.updateBalance(request.getSourceAccount(), new BalanceRequest(-request.getAmount()));
            accountClient.updateBalance(request.getDestinationAccount(), new BalanceRequest(request.getAmount()));

            logTransaction("TRANSFER", request.getAmount(), "SUCCESS", request.getSourceAccount(), request.getDestinationAccount());

            notificationClient.sendNotification("Transfer of " + request.getAmount() + " successful.");
            return "Transfer successful";

        } catch (Exception e) {
            log.error("Transfer failed: {}", e.getMessage());
            logTransaction("TRANSFER", request.getAmount(), "FAILED", request.getSourceAccount(), request.getDestinationAccount());
            throw e;
        }
    }

    public List<Transaction> getTransactions(String accountNumber) {
        log.info("Fetching transactions for account: {}", accountNumber);
        return transactionRepository.findBySourceAccountOrDestinationAccount(accountNumber, accountNumber);
    }

    //  HELPER METHODS

    private Transaction logTransaction(String type, double amount, String status, String accountNumber) {
        String txnId = generateTransactionId();
        Transaction txn = new Transaction(txnId, type, amount, status, accountNumber);
        return transactionRepository.save(txn);
    }

    private void logTransaction(String type, double amount, String status, String source, String dest) {
        String txnId = generateTransactionId();
        Transaction txn = new Transaction(txnId, amount, status, source, dest);
        transactionRepository.save(txn);
    }

    private String generateTransactionId() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int randomSuffix = ThreadLocalRandom.current().nextInt(100, 999);
        return "TXN-" + timestamp + "-" + randomSuffix;
    }

    //  FALLBACK METHODS
    public Transaction fallbackTransaction(TransactionRequest request, Throwable t) {
        log.error("Fallback: Account Service is down. Reason: {}", t.getMessage());
        Transaction txn = new Transaction();
        txn.setAmount(request.getAmount());
        txn.setStatus("FAILED - SERVICE UNAVAILABLE");
        txn.setTimestamp(LocalDateTime.now());
        return txn;
    }

    public String fallbackTransfer(TransferRequest request, Throwable t) {
        log.error("Fallback Transfer: Account Service is down. Reason: {}", t.getMessage());
        return "Transfer Failed: Account Service is unavailable.";
    }
}
