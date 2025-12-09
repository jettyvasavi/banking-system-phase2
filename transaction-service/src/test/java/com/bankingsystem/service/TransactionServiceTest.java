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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountClient accountClient;
    @Mock private NotificationClient notificationClient;

    @InjectMocks private TransactionService transactionService;

    private AccountDTO mockAccount;

    @BeforeEach
    void setUp() {
        mockAccount = new AccountDTO();
        mockAccount.setAccountNumber("ACC123");
        mockAccount.setBalance(1000.0);
        mockAccount.setStatus("ACTIVE");
    }

    // --- 1. Deposit Tests ---
    @Test
    void testDeposit_Success() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("ACC123");
        request.setAmount(500.0);

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction txn = transactionService.deposit(request);

        assertEquals("SUCCESS", txn.getStatus());
        assertEquals("DEPOSIT", txn.getType());
        verify(accountClient).updateBalance(eq("ACC123"), any(BalanceRequest.class));
        verify(notificationClient).sendNotification(anyString());
    }

    // --- 2. Withdraw Tests ---
    @Test
    void testWithdraw_Success() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("ACC123");
        request.setAmount(500.0);

        when(accountClient.getAccount("ACC123")).thenReturn(mockAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction txn = transactionService.withdraw(request);

        assertEquals("SUCCESS", txn.getStatus());
        assertEquals("WITHDRAW", txn.getType());
        verify(accountClient).updateBalance(eq("ACC123"), any(BalanceRequest.class));
    }

    @Test
    void testWithdraw_InsufficientFunds() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("ACC123");
        request.setAmount(5000.0); // More than 1000

        when(accountClient.getAccount("ACC123")).thenReturn(mockAccount);
        // We mock save() because our service now logs "FAILED" before throwing exception
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        assertThrows(InsufficientBalanceException.class, () -> transactionService.withdraw(request));

        // Verify we logged a FAILED transaction
        verify(transactionRepository).save(argThat(t -> t.getStatus().equals("FAILED")));
    }

    // --- 3. Transfer Tests ---
    @Test
    void testTransfer_Success() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccount("ACC123");
        request.setDestinationAccount("ACC999");
        request.setAmount(100.0);

        when(accountClient.getAccount("ACC123")).thenReturn(mockAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        String result = transactionService.transfer(request);

        assertEquals("Transfer successful", result);
        verify(accountClient, times(2)).updateBalance(anyString(), any(BalanceRequest.class));
    }

    @Test
    void testTransfer_SameAccount() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccount("ACC123");
        request.setDestinationAccount("ACC123"); // Same
        request.setAmount(100.0);

        assertThrows(InvalidAccountException.class, () -> transactionService.transfer(request));
        verify(accountClient, never()).updateBalance(anyString(), any());
    }
}
