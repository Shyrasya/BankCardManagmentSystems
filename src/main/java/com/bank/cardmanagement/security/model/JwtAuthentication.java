package com.bank.cardmanagement.security.model;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * Реализация интерфейса Authentication, представляющая собой аутентификацию через JWT токен.
 * Содержит информацию о пользователе (ID) и его роли.
 */
public class JwtAuthentication implements Authentication {

    /**
     * ID пользователя, связанный с аутентификацией.
     */
    private final Long id;

    /**
     * Роль пользователя в системе.
     */
    private final GrantedAuthority role;

    /**
     * Статус аутентификации пользователя.
     */
    private boolean isAuthenticated;

    /**
     * Конструктор для создания объекта JwtAuthentication.
     *
     * @param id   уникальный идентификатор пользователя
     * @param role роль пользователя в системе
     */
    public JwtAuthentication(Long id, GrantedAuthority role) {
        this.id = id;
        this.role = role;
        this.isAuthenticated = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return id;
    }

    @Override
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.isAuthenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return Long.toString(id);
    }
}
