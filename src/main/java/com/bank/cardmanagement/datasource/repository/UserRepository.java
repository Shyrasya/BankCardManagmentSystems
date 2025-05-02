package com.bank.cardmanagement.datasource.repository;

import com.bank.cardmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link User}.
 * Предоставляет методы для поиска и модификации данных пользователя в базе данных.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Проверяет, существует ли пользователь с указанным адресом электронной почты.
     *
     * @param email адрес электронной почты пользователя.
     * @return {@code true}, если пользователь с таким email существует, иначе {@code false}.
     */
    boolean existsByEmail(String email);

    /**
     * Находит пользователя по указанному адресу электронной почты.
     *
     * @param email адрес электронной почты пользователя.
     * @return {@link Optional} с найденным пользователем, если таковой существует, иначе {@link Optional#empty()}.
     */
    Optional<User> findByEmail(String email);

    /**
     * Обновляет токен обновления (refresh token) для пользователя по его ID.
     *
     * @param id           идентификатор пользователя.
     * @param refreshToken новый токен обновления.
     */
    @Modifying
    @Query("UPDATE User u SET u.refreshToken = :refreshToken WHERE u.id = :id")
    void updateRefreshTokenByUuid(@Param("id") Long id, @Param("refreshToken") String refreshToken);

    /**
     * Удаляет токен обновления (refresh token) для пользователя по его ID.
     *
     * @param id идентификатор пользователя.
     */
    @Modifying
    @Query("UPDATE User u SET u.refreshToken = NULL WHERE u.id =:id")
    void deleteRefreshTokenByUuid(@Param("id") Long id);

    /**
     * Удаляет пользователя по указанному адресу электронной почты.
     *
     * @param email адрес электронной почты пользователя.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.email = :email")
    void deleteByEmail(@Param("email") String email);
}
