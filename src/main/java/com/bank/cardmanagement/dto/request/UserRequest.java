package com.bank.cardmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRequest {
    @NotBlank(message = "Email не должен быть пустым!")
    @Email(message = "Некорректный формат email!")
    private String email;

    @NotBlank(message = "Пароль не должен быть пустым!")
    @Size(min = 4, message = "Пароль должен содержать минимум 4 символа!")
    private String password;

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
}
