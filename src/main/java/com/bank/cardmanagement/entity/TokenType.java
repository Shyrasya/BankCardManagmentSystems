package com.bank.cardmanagement.entity;

/**
 * Перечисление типов токенов, используемых для аутентификации и авторизации.
 * Представляет собой два типа токенов: access-token и refresh-token.
 */
public enum TokenType {
    /**
     * Токен доступа (Access Token).
     * Используется для доступа к защищённым ресурсам.
     */
    ACCESS,

    /**
     * Токен обновления (Refresh Token).
     * Используется для получения нового токена доступа.
     */
    REFRESH
}
