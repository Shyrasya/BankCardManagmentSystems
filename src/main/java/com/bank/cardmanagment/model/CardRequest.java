package com.bank.cardmanagment.model;

import jakarta.validation.constraints.NotNull;

public class CardRequest {

    @NotNull(message = "ID пользователя обязателен")
    private Long userId;


    public CardRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
