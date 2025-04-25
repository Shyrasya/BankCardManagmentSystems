package com.bank.cardmanagment.model;

import jakarta.validation.constraints.NotBlank;

public class TokenRequest {

    @NotBlank(message = "Refresh-токен обязателен")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
