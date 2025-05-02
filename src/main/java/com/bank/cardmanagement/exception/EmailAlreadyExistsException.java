package com.bank.cardmanagement.exception;

/**
 * Исключение, которое выбрасывается, когда электронная почта уже существует в системе.
 * Наследует {@link RuntimeException}.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    /**
     * Конструктор для создания исключения с заданным сообщением.
     *
     * @param message Сообщение, описывающее причину возникновения исключения
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
