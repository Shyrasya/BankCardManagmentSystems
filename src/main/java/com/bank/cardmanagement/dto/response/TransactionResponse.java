package com.bank.cardmanagement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ответ, содержащий информацию о транзакции.
 * Используется для возврата данных о транзакции в ответах API.
 */
public class TransactionResponse {

    /**
     * Уникальный идентификатор транзакции.
     */
    private Long id;

    /**
     * Тип транзакции.
     */
    private String type;

    /**
     * Сумма транзакции.
     * Отображает величину средств, задействованных в транзакции.
     */
    private BigDecimal amount;

    /**
     * Описание транзакции.
     * Может содержать дополнительную информацию о транзакции.
     */
    private String description;


    /**
     * Время выполнения транзакции.
     * Содержит временную метку, когда транзакция была выполнена.
     */
    private LocalDateTime timestamp;

    /**
     * Конструктор для создания объекта ответа о транзакции.
     *
     * @param id          Уникальный идентификатор транзакции.
     * @param type        Тип транзакции.
     * @param amount      Сумма транзакции.
     * @param description Описание транзакции.
     * @param timestamp   Время выполнения транзакции.
     */
    public TransactionResponse(Long id, String type, BigDecimal amount, String description, LocalDateTime timestamp) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
