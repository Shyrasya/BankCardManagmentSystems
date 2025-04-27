package com.bank.cardmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CardRequest {

    @NotNull(message = "ID пользователя обязателен!")
    @Positive(message = "ID пользователя должен быть положительным числом!")
    private Long userId;


    public CardRequest(Long userId) {
        this.userId = userId;
    }

    public CardRequest() {}


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
