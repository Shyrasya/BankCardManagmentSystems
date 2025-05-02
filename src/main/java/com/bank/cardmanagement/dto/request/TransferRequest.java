package com.bank.cardmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Класс запроса на перевод средств между картами.
 * Используется для передачи данных о переводе с одной банковской карты на другую.
 */
public class TransferRequest {

    /**
     * ID карты-отправителя.
     * Значение обязательно и должно быть положительным числом.
     */
    @NotNull
    @Positive
    private Long sourceCardId;

    /**
     * ID карты-получателя.
     * Значение обязательно и должно быть положительным числом.
     */
    @NotNull
    @Positive
    private Long destinationCardId;

    /**
     * Сумма перевода.
     * Значение обязательно и должно быть положительным числом.
     */
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
