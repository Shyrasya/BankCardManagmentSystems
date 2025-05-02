package com.bank.cardmanagement.security;

import com.bank.cardmanagement.security.model.JwtAuthentication;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Утилитный класс для работы с JWT токенами, в частности для создания аутентификации на основе
 * информации, содержащейся в токене (claims).
 */
public class JwtUtil {

    /**
     * Создаёт объект аутентификации (JwtAuthentication) на основе полезной нагрузки (claims) из JWT токена.
     * Извлекаются ID пользователя и его роль для создания авторизации в приложении.
     *
     * @param claims полезная нагрузка (claims) JWT токена, содержащая информацию о пользователе
     * @return JwtAuthentication объект с данными пользователя и его ролью
     */
    public JwtAuthentication createAuthentication(Claims claims) {
        Long id = Long.parseLong(claims.getSubject());
        String role = claims.get("role", String.class);
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
        return new JwtAuthentication(id, authority);
    }
}
