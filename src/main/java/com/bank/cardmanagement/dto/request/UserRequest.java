package com.bank.cardmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос на создание или обновление пользователя.
 * Этот класс используется для передачи данных, необходимых для регистрации нового пользователя или обновления существующего.
 */
public class UserRequest {

    /**
     * Электронная почта пользователя.
     * Должна быть валидным email-адресом и не может быть пустым.
     */
    @NotBlank(message = "Email не должен быть пустым!")
    @Email(message = "Некорректный формат email!")
    private String email;

    /**
     * Пароль пользователя.
     * Пароль не может быть пустым и должен содержать минимум 4 символа.
     */
    @NotBlank(message = "Пароль не должен быть пустым!")
    @Size(min = 4, message = "Пароль должен содержать минимум 4 символа!")
    private String password;

    /**
     * Роль пользователя.
     * Роль не может быть пустой и должна быть задана для пользователя.
     */
    @NotBlank(message = "Роль не должна быть пустой!")
    private String role;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
