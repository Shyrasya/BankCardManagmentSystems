package com.bank.cardmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Класс запроса на создание банковской карты.
 * Содержит идентификатор пользователя-владельца карты.
 */
public class CardRequest {

    /**
     * Идентификатор пользователя, которому принадлежит карта.
     * Не может быть null и должен быть положительным числом.
     */
    @NotNull(message = "ID пользователя обязателен!")
    @Positive(message = "ID пользователя должен быть положительным числом!")
    private Long userId;

    /**
     * Конструктор с параметром.
     *
     * @param userId ID пользователя
     */
    public CardRequest(Long userId) {
        this.userId = userId;
    }

    /**
     * Конструктор без параметров.
     */
    public CardRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
