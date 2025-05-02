package com.bank.cardmanagement.exception;

/**
 * Исключение, которое выбрасывается, когда пользователь не найден в системе.
 * Наследует {@link RuntimeException}.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Конструктор для создания исключения с заданным сообщением.
     *
     * @param message Сообщение, описывающее причину возникновения исключения
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
