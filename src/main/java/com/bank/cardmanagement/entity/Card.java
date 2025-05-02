package com.bank.cardmanagement.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность банковской карты.
 * Содержит информацию о карте, её владельце, балансе, статусе, лимитах и связанных транзакциях.
 */
@Entity
@Table(name = "cards")
public class Card {

    /**
     * Уникальный идентификатор карты.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Зашифрованный номер карты.
     * Уникальный и обязательный для каждой карты.
     */
    @Column(name = "encrypted_card_number", nullable = false, unique = true, length = 255)
    private String encryptedCardNumber;

    /**
     * Пользователь, которому принадлежит карта.
     * Связь "много к одному" с сущностью User.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Дата истечения срока действия карты.
     * Обязательное поле.
     */
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    /**
     * Статус карты (ACTIVE, BLOCKED, EXPIRED).
     * Указывает на текущее состояние карты.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status;

    /**
     * Баланс карты.
     * Хранит сумму на счете карты.
     */
    @Column(precision = 19, scale = 2)
    private BigDecimal balance;

    /**
     * Ежедневный лимит карты.
     * Определяет максимальную сумму, которую можно снять с карты за день.
     */
    @Column(name = "daily_limit", nullable = false)
    private BigDecimal dailyLimit = BigDecimal.valueOf(100_000.00);

    /**
     * Ежемесячный лимит карты.
     * Определяет максимальную сумму, которую можно снять с карты за месяц.
     */
    @Column(name = "monthly_limit", nullable = false)
    private BigDecimal monthlyLimit = BigDecimal.valueOf(1_000_000.00);

    /**
     * Список транзакций, связанных с картой.
     * Связь "один ко многим" с сущностью Transaction.
     */
    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    /**
     * Базовый констуктор для создания карты
     */
    public Card() {
    }

    /**
     * Конструктор для создания карты с указанными параметрами.
     *
     * @param user                Пользователь, которому принадлежит карта.
     * @param encryptedCardNumber Зашифрованный номер карты.
     * @param expirationDate      Дата истечения срока действия карты.
     * @param status              Статус карты.
     * @param balance             Баланс карты.
     * @param dailyLimit          Ежедневный лимит.
     * @param monthlyLimit        Ежемесячный лимит.
     */
    public Card(User user,
                String encryptedCardNumber,
                LocalDate expirationDate,
                CardStatus status,
                BigDecimal balance,
                BigDecimal dailyLimit,
                BigDecimal monthlyLimit) {
        this.user = user;
        this.encryptedCardNumber = encryptedCardNumber;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance;
        this.dailyLimit = dailyLimit != null ? dailyLimit : BigDecimal.valueOf(100_000.00);
        this.monthlyLimit = monthlyLimit != null ? monthlyLimit : BigDecimal.valueOf(1_000_000.00);
    }

    public void setEncryptedCardNumber(String encryptedCardNumber) {
        this.encryptedCardNumber = encryptedCardNumber;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public CardStatus getStatus() {
        return status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getEncryptedCardNumber() {
        return encryptedCardNumber;
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
