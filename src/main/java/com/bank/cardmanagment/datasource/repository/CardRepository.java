package com.bank.cardmanagment.datasource.repository;

import com.bank.cardmanagment.model.Card;
import com.bank.cardmanagment.model.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {

    boolean existsByEncryptedCardNumber(String encryptedCardNumber);

    Page<Card> findByUserId(Long userId, Pageable pageable);

    Page<Card> findByStatus(CardStatus status, Pageable pageable);

    Page<Card> findByStatusAndUserId(CardStatus status, Long userId, Pageable pageable);

    boolean existsByIdAndUserId(Long id, Long userId);
}
