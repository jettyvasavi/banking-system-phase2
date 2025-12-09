package com.bankingsystem.client;

import com.bankingsystem.dto.AccountDTO;
import com.bankingsystem.dto.BalanceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service")
public interface AccountClient {

    @GetMapping("/api/accounts/{accountNumber}")
    AccountDTO getAccount(@PathVariable String accountNumber);

    @PutMapping("/api/accounts/{accountNumber}/balance")
    void updateBalance(@PathVariable String accountNumber, @RequestBody BalanceRequest request);
}