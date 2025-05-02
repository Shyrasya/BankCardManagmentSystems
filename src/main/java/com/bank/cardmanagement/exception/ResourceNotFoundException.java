package com.bank.cardmanagement.exception;

/**
 * Исключение, которое выбрасывается, когда запрашиваемый ресурс не найден.
 * Наследует {@link RuntimeException}.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Конструктор для создания исключения с заданным сообщением.
     *
     * @param message Сообщение, описывающее причину возникновения исключения
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}