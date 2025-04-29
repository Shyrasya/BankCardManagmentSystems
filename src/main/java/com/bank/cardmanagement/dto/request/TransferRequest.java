package com.bank.cardmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class TransferRequest {

    @NotNull
    @Positive
    private Long sourceCardId;

    @NotNull
    @Positive
    private Long destinationCardId;

    @NotNull
    @Positive
    private BigDecimal amount;

    public Long getSourceCardId() {
        return sourceCardId;
    }

    public Long getDestinationCardId() {
        return destinationCardId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setSourceCardId(Long sourceCardId) {
        this.sourceCardId = sourceCardId;
    }

    public void setDestinationCardId(Long destinationCardId) {
        this.destinationCardId = destinationCardId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
