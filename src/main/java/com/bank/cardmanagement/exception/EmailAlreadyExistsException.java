package com.bank.cardmanagement.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException (String message){
        super(message);
    }
}
