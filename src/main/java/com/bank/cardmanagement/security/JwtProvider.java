package com.bank.cardmanagement.security;

import com.bank.cardmanagement.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * Класс для работы с JWT токенами: генерация, валидация и извлечение информации из токенов.
 * Используется для аутентификации и авторизации пользователей через access и refresh токены.
 */
public class JwtProvider {

    /**
     * Время жизни access токена в миллисекундах.
     */
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * Время жизни refresh токена в миллисекундах.
     */
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * Секретный ключ для подписи токенов.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Секретный ключ для создания подписи JWT.
     */
    private SecretKey secretKey;

    /**
     * Инициализация секретного ключа для подписи токенов.
     * Вызывается при старте приложения после инъекций зависимостей.
     */
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Генерация access токена для пользователя.
     *
     * @param user пользователь, для которого генерируется токен
     * @return сгенерированный access токен
     */
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusMillis(accessTokenExpiration);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expirationTime))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Генерация refresh токена для пользователя.
     *
     * @param user пользователь, для которого генерируется токен
     * @return сгенерированный refresh токен
     */
    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusMillis(refreshTokenExpiration);

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expirationTime))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Валидация access токена.
     *
     * @param accessToken токен для валидации
     * @return Claims (полезная нагрузка) из токена, если токен валиден
     */
    public Claims validateAccessToken(String accessToken) {
        try {
            return validateToken(accessToken);
        } catch (SecurityException e) {
            return null;
        }
    }

    /**
     * Валидация refresh токена.
     *
     * @param user         пользователь, чей refresh токен проверяется
     * @param refreshToken refresh токен для валидации
     * @return true, если refresh токен совпадает с сохранённым в базе
     */
    public boolean validateRefreshToken(User user, String refreshToken) {
        return Objects.equals(user.getRefreshToken(), refreshToken);
    }

    /**
     * Общая валидация токена (access и refresh).
     *
     * @param token токен для валидации
     * @return Claims (полезная нагрузка) из токена, если токен валиден
     * @throws SecurityException если токен невалиден
     */
    public Claims validateToken(String token) {
        try {
            return getClaims(token);
        } catch (SecurityException e) {
            throw new SecurityException("Неверная подпись JWT!", e);
        } catch (MalformedJwtException e) {
            throw new SecurityException("Некорректный JWT!", e);
        } catch (JwtException e) {
            throw new SecurityException("Ошибка в JWT!", e);
        }
    }

    /**
     * Извлечение полезной нагрузки (claims) из токена.
     *
     * @param token токен для извлечения информации
     * @return Claims (полезная нагрузка) из токена
     * @throws SecurityException если произошла ошибка при извлечении данных
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (Exception e) {
            throw new SecurityException("Ошибка при декодировании токена", e);
        }
    }

    /**
     * Проверка свежести токена на основе времени его истечения.
     *
     * @param claims полезная нагрузка токена
     * @return true, если токен ещё действителен
     */
    public boolean isFreshToken(Claims claims) {
        return claims.getExpiration().after(Date.from(Instant.now()));
    }
}
