package com.bank.cardmanagement.dto.response;

import java.math.BigDecimal;

/**
 * Ответ, содержащий информацию о банковской карте.
 * Этот класс используется для передачи данных о карте в ответах API.
 */
public class CardResponse {

    /**
     * Уникальный идентификатор карты.
     */
    private Long id;

    /**
     * Маскированный номер карты.
     * Хранит только часть номера карты, скрывая другие цифры для безопасности.
     */
    private String maskedCardNumber;

    /**
     * Дата истечения срока действия карты в формате строки.
     * Используется для указания, когда карта становится недействительной.
     */
    private String expirationDate;

    /**
     * Статус карты (например, ACTIVE, BLOCKED, EXPIRED).
     * Отражает текущее состояние карты.
     */
    private String status;

    /**
     * Баланс на карте.
     * Хранит текущую сумму средств, доступных для использования.
     */
    private BigDecimal balance;

    /**
     * Идентификатор пользователя, которому принадлежит карта.
     * Указывает, кто является владельцем карты.
     */
    private Long userId;

    /**
     * Конструктор для создания объекта CardResponse с заданными параметрами.
     *
     * @param id               Уникальный идентификатор карты.
     * @param maskedCardNumber Маскированный номер карты.
     * @param expirationDate   Дата истечения срока действия карты.
     * @param status           Статус карты.
     * @param balance          Баланс на карте.
     * @param userId           Идентификатор пользователя, которому принадлежит карта.
     */
    public CardResponse(Long id, String maskedCardNumber, String expirationDate, String status, BigDecimal balance, Long userId) {
        this.id = id;
        this.maskedCardNumber = maskedCardNumber;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Long getUserId() {
        return userId;
    }
}
