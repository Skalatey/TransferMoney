package com.trn.dto;

public class TransferRequestDto {
    private Long idToAccount;

    private Long idFromAccount;

    private double amount;

    public TransferRequestDto() {
    }

    public Long getIdToAccount() {
        return idToAccount;
    }

    public void setIdToAccount(Long idToAccount) {
        this.idToAccount = idToAccount;
    }

    public Long getIdFromAccount() {
        return idFromAccount;
    }

    public void setIdFromAccount(Long idFromAccount) {
        this.idFromAccount = idFromAccount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
