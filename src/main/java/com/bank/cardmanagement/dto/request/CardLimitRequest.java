package com.bank.cardmanagement.dto.request;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class CardLimitRequest {
    @PositiveOrZero(message = "Дневной лимит не может быть отрицательным!")
    private BigDecimal dailyLimit;

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
