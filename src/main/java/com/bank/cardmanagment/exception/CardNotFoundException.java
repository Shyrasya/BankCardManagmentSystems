package com.bank.cardmanagment.exception;

public class CardNotFoundException extends RuntimeException{
    public CardNotFoundException (String message){
        super(message);
    }

}
