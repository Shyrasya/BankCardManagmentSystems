package com.bank.cardmanagment.model;

import java.math.BigDecimal;

public class CardResponse {
    private Long id;
    private String maskedCardNumber;

    private String expirationDate;
    private String status;
    private BigDecimal balance;

    public CardResponse(Long id, String maskedCardNumber, String expirationDate, String status, BigDecimal balance) {
        this.id = id;
        this.maskedCardNumber = maskedCardNumber;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance;
    }
}
