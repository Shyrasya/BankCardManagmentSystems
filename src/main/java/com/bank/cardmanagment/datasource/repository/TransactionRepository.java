package com.bank.cardmanagment.datasource.repository;

import com.bank.cardmanagment.model.Transaction;
import com.bank.cardmanagment.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByTypeAndCardId(TransactionType transactionType, Long cardId, Pageable pageable);

    Page<Transaction> findByType(TransactionType transactionType, Pageable pageable);

    Page<Transaction> findByCardId(Long cardId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.type = :transactionType AND t.card.user.id = :userId")
    Page<Transaction> findByTypeAndUserId(@Param("transactionType") TransactionType transactionType,
                                          @Param("userId") Long UserId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.card.user.id = :userId")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
