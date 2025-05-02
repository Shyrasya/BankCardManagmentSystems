package com.bank.cardmanagement.exception;

/**
 * Исключение, которое выбрасывается, когда запрашиваемая карта не найдена.
 * Наследует {@link RuntimeException}.
 */
public class CardNotFoundException extends RuntimeException {

    /**
     * Конструктор для создания исключения с заданным сообщением.
     *
     * @param message Сообщение, описывающее причину возникновения исключения
     */
    public CardNotFoundException(String message) {
        super(message);
    }
}
