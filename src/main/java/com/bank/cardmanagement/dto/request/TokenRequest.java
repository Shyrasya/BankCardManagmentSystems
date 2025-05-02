package com.bank.cardmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Класс запроса для обновления JWT-токена.
 * Используется при запросах на обновление access- или refresh-токенов.
 */
public class TokenRequest {

    /**
     * Refresh-токен, используемый для генерации нового access- или refresh-токена.
     * Не должен быть пустым.
     */
    @NotBlank(message = "Refresh-токен обязателен")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
