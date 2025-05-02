package com.bank.cardmanagement.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность транзакции для банковской карты.
 * Представляет собой запись о финансовой операции, связанной с картой.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    /**
     * Уникальный идентификатор транзакции.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Тип транзакции (например, создание, блокировка, снятие средств и т. д.).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    /**
     * Сумма транзакции.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Описание транзакции (например, причина снятия средств или описание перевода).
     */
    @Column(length = 255)
    private String description;

    /**
     * Время, когда транзакция была совершена.
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Карта, с которой была произведена транзакция.
     */
    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    /**
     * Конструктор для создания новой транзакции.
     *
     * @param type        тип транзакции
     * @param amount      сумма транзакции
     * @param description описание транзакции
     * @param timestamp   время транзакции
     * @param card        карта, с которой была совершена транзакция
     */
    public Transaction(TransactionType type, BigDecimal amount, String description, LocalDateTime timestamp, Card card) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
        this.card = card;
    }

    /**
     * Конструктор без параметров.
     */
    public Transaction() {
    }

    public Long getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setCard(Card card) {
        this.card = card;
    }
}
