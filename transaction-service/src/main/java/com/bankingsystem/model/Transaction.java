package com.bankingsystem.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    private String transactionId;
    private String type;          // DEPOSIT, WITHDRAW, TRANSFER
    private Double amount;
    private String sourceAccount;
    private String destinationAccount;
    private String status;        // SUCCESS, FAILED
    private LocalDateTime timestamp;

    // Constructor for Deposit/Withdraw
    public Transaction(String transactionId, String type, double amount, String status, String accountNumber) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now(); // Updated to Java 8 Time API
        this.status = status;

        if ("DEPOSIT".equalsIgnoreCase(type)) {
            this.destinationAccount = accountNumber;
        } else if ("WITHDRAW".equalsIgnoreCase(type)) {
            this.sourceAccount = accountNumber;
        }
    }

    // Constructor for Transfer
    public Transaction(String transactionId, double amount, String status, String sourceAccount, String destinationAccount) {
        this.transactionId = transactionId;
        this.type = "TRANSFER";
        this.amount = amount;
        this.timestamp = LocalDateTime.now(); // Updated to Java 8 Time API
        this.status = status;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
    }

    public Transaction() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
