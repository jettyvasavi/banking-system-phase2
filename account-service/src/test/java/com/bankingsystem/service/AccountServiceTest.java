package com.bankingsystem.service;

import com.bankingsystem.exception.AccountNotFoundException;
import com.bankingsystem.model.Account;
import com.bankingsystem.model.dto.CreateAccountRequest;
import com.bankingsystem.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account mockAccount;

    @BeforeEach
    void setUp() {
        mockAccount = new Account("S1234", "John Doe");
        mockAccount.setId("1");
        mockAccount.setBalance(1000.0);
        mockAccount.setStatus("ACTIVE");
    }

    // --- 1. Create Account Tests ---
    @Test
    void testCreateAccount_Success() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setHolderName("John Doe");

          when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);

        Account created = accountService.createAccount(request);

        assertNotNull(created);
        assertEquals("John Doe", created.getHolderName());
        assertEquals(0.0, created.getBalance());
        assertEquals("ACTIVE", created.getStatus());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    // --- 2. Get Account Tests ---
    @Test
    void testGetAccount_Success() {
        when(accountRepository.findByAccountNumber("S1234")).thenReturn(Optional.of(mockAccount));

        Account found = accountService.getAccount("S1234");
        assertEquals("S1234", found.getAccountNumber());
    }

    @Test
    void testGetAccount_NotFound() {
        when(accountRepository.findByAccountNumber("XXXX")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount("XXXX"));
    }

    // --- 3. Update Balance Tests ---
    @Test
    void testUpdateBalance_Success() {
        when(accountRepository.findByAccountNumber("S1234")).thenReturn(Optional.of(mockAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);

        Account updated = accountService.updateBalance("S1234", 500.0);
        assertEquals(1500.0, updated.getBalance()); // 1000 + 500
    }

    @Test
    void testUpdateBalance_InactiveAccount() {
        mockAccount.setStatus("INACTIVE");
        when(accountRepository.findByAccountNumber("S1234")).thenReturn(Optional.of(mockAccount));

        assertThrows(RuntimeException.class, () -> accountService.updateBalance("S1234", 100.0));
        verify(accountRepository, never()).save(any(Account.class));
    }

    // --- 4. Update Status Tests ---
    @Test
    void testUpdateStatus_Success() {
        when(accountRepository.findByAccountNumber("S1234")).thenReturn(Optional.of(mockAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);

        Account updated = accountService.updateStatus("S1234", "INACTIVE");
        assertEquals("INACTIVE", updated.getStatus());
    }

    @Test
    void testUpdateStatus_InvalidStatus() {
        when(accountRepository.findByAccountNumber("S1234")).thenReturn(Optional.of(mockAccount));
        assertThrows(RuntimeException.class, () -> accountService.updateStatus("S1234", "DELETED"));
    }
}
