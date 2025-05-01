package com.bank.cardmanagement.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encrypted_card_number", nullable = false, unique = true, length = 255)
    private String encryptedCardNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "daily_limit", nullable = false)
    private BigDecimal dailyLimit = BigDecimal.valueOf(100_000.00);

    @Column(name = "monthly_limit", nullable = false)
    private BigDecimal monthlyLimit = BigDecimal.valueOf(1_000_000.00);


    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    public Card() {
    }

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
