package com.bank.cardmanagement.dto.response;

/**
 * Ответ, содержащий данные JWT-токенов.
 * Используется для возврата информации о токенах авторизации (Access Token и Refresh Token) в ответах API.
 */
public class JwtResponse {

    /**
     * Тип токена, по умолчанию "Bearer".
     * Указывает, что токен является Bearer-токеном, который передается в заголовке Authorization.
     */
    private final String type = "Bearer";

    /**
     * Токен доступа для авторизации запросов пользователя.
     * Используется для доступа к защищенным ресурсам.
     */
    private String accessToken;

    /**
     * Токен обновления, используется для получения нового Access Token.
     */
    private String refreshToken;

    /**
     * Время истечения действия Access Token (в миллисекундах).
     * Указывает, через какое время токен станет недействительным.
     */
    private long expiresIn;

    /**
     * Конструктор без параметров.
     * Используется для создания пустого объекта JwtResponse.
     */
    public JwtResponse() {
    }

    /**
     * Конструктор для создания объекта JwtResponse с указанием токенов и времени истечения.
     *
     * @param accessToken  Токен доступа (Access Token).
     * @param refreshToken Токен обновления (Refresh Token).
     * @param expiresIn    Время истечения действия Access Token в миллисекундах.
     */
    public JwtResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getType() {
        return type;
    }
}
