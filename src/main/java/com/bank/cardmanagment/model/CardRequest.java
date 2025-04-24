package com.bank.cardmanagment.model;

public class CardRequest {

    private Long userId;


    public CardRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
