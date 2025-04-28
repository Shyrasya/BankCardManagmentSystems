package com.bank.cardmanagement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;

    private String type;

    private BigDecimal amount;

    private String description;

    private LocalDateTime timestamp;

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
