package com.bank.cardmanagement.exception;

public class CardNotFoundException extends RuntimeException{
    public CardNotFoundException (String message){
        super(message);
    }

}
