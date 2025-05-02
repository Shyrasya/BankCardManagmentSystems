package com.bank.cardmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Запрос на снятие средств с банковской карты.
 * Этот класс используется для передачи данных, необходимых для выполнения операции снятия наличных.
 */
public class WithdrawRequest {

    /**
     * Сумма, которую необходимо снять с карты.
     * Должна быть положительным числом и не может быть равной нулю.
     */
    @NotNull
    @Positive
    private BigDecimal amount;

    /**
     * Описание операции снятия средств.
     * Необязательное поле для добавления дополнительной информации о транзакции.
     */
    private String description;

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
