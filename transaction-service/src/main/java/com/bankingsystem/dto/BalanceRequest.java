package com.bankingsystem.dto;

import jakarta.validation.constraints.NotNull;

public class BalanceRequest {

    @NotNull(message = "Amount cannot be null")
    private Double amount;
    public BalanceRequest() {}

    public BalanceRequest(Double amount) {
        this.amount = amount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
