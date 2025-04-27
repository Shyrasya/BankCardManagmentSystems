package com.bank.cardmanagement.datasource.repository;

import com.bank.cardmanagement.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.refreshToken = :refreshToken WHERE u.id = :id")
    void updateRefreshTokenByUuid(@Param("id") Long id, @Param("refreshToken") String refreshToken);

    @Modifying
    @Query("UPDATE User u SET u.refreshToken = NULL WHERE u.id =:id")
    void deleteRefreshTokenByUuid(@Param("id") Long id);
}
