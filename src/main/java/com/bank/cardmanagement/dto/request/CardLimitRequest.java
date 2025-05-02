package com.bank.cardmanagement.dto.request;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Класс запроса на установку лимитов банковской карты.
 * Содержит дневной и месячный лимиты. Оба значения не могут быть отрицательными.
 */
public class CardLimitRequest {

    /**
     * Дневной лимит по карте.
     * Может быть ноль или положительное значение.
     */
    @PositiveOrZero(message = "Дневной лимит не может быть отрицательным!")
    private BigDecimal dailyLimit;

    /**
     * Месячный лимит по карте.
     * Может быть ноль или положительное значение.
     */
    @PositiveOrZero(message = "Месячный лимит не может быть отрицательным!")
    private BigDecimal monthlyLimit;

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }
}
