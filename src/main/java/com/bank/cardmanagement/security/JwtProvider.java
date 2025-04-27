package com.bank.cardmanagement.security;

import com.bank.cardmanagement.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

public class JwtProvider {
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.secret}")
    private String secret;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

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

    public Claims validateAccessToken(String accessToken) {
        try {
            return validateToken(accessToken);
        } catch (SecurityException e) {
            return null;
        }
    }

    public boolean validateRefreshToken(User user, String refreshToken) {
        String userRefreshToken = user.getRefreshToken();
        return userRefreshToken.equals(refreshToken);
    }

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

    public boolean isFreshToken(Claims claims) {
        return claims.getExpiration().after(Date.from(Instant.now()));
    }
}
