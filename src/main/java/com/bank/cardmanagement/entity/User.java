package com.bank.cardmanagement.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Сущность пользователя в системе.
 * Представляет пользователя, включая его электронную почту, пароль, роль и связанный набор карт.
 * Сопоставляется с таблицей {@code users} в базе данных.
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Уникальный идентификатор пользователя.
     * Генерируется автоматически с помощью стратегии {@link GenerationType.IDENTITY}.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Электронная почта пользователя.
     * Поле не может быть {@code null} и должно быть уникальным.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Пароль пользователя.
     * Поле не может быть {@code null}.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Роль пользователя.
     * Определяется с помощью {@link Role} и не может быть {@code null}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Токен обновления для пользователя.
     * Может быть {@code null}.
     */
    @Column(name = "refresh_token")
    private String refreshToken;

    /**
     * Набор карт, связанных с пользователем.
     * Все изменения в {@link Set<Card>} будут каскадированы (например, удаление карт).
     * Связь отображена в сущности {@link Card} с полем {@code user}.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Card> cards = new HashSet<>();

    /**
     * Конструктор без параметров для JPA.
     */
    public User() {
    }

    /**
     * Конструктор для создания нового пользователя с указанной электронной почтой, паролем и ролью.
     *
     * @param email    Электронная почта пользователя.
     * @param password Пароль пользователя.
     * @param role     Роль пользователя.
     */
    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }
}
