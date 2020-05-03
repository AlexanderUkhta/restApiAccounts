package com.api.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

public class TransactionDto {
    public TransactionDto() {

    }
    public TransactionDto (String ownerName, Double amount) {
        this.ownerName = ownerName;
        this.amount = amount;
    }

    @NotNull(message = "'amount' should be present")
    @PositiveOrZero(message = "'amount' should not be negative")
    private Double amount;
    private Integer accountMain;
    private Integer accountExternal;
    private String ownerName;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getAccountMain() {
        return accountMain;
    }

    public void setAccountMain(Integer accountMain) {
        this.accountMain = accountMain;
    }

    public Integer getAccountExternal() {
        return accountExternal;
    }

    public void setAccountExternal(Integer accountExternal) {
        this.accountExternal = accountExternal;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public String toString() {
        return "accountMain: " + accountMain +
                " accountExternal: " + accountExternal +
                " ownerName: " + ownerName +
                " amount: " + amount;
    }
}
