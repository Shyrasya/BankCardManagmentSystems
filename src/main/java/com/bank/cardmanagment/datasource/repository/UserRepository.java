package com.bank.cardmanagment.datasource.repository;

import com.bank.cardmanagment.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    boolean existsByLogin(String login);

    Optional<User> findByLogin(String login);

    Optional<User> findById(Long id);

    @Modifying
    @Query("UPDATE User u SET u.refreshToken = :refreshToken WHERE u.id = :id")
    void updateRefreshTokenByUuid(@Param("id") Long id, @Param("refreshToken") String refreshToken);

    @Modifying
    @Query("UPDATE User u SET u.refreshToken = NULL WHERE u.id =:id")
    void deleteRefreshTokenByUuid(@Param("id") Long id);
}
