package com.bank.cardmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Класс запроса аутентификации пользователя (вход в систему).
 * Используется для передачи email и пароля.
 */
public class JwtRequest {

    /**
     * Email пользователя. Не должен быть пустым.
     */
    @NotBlank(message = "Email обязателен")
    private String email;

    /**
     * Пароль пользователя. Не должен быть пустым.
     */
    @NotBlank(message = "Пароль обязателен")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
