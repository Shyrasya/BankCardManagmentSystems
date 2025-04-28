package com.bank.cardmanagement.datasource.repository;

import com.bank.cardmanagement.entity.Transaction;
import com.bank.cardmanagement.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByTypeAndCardId(TransactionType transactionType, Long cardId, Pageable pageable);

    Page<Transaction> findByType(TransactionType transactionType, Pageable pageable);

    Page<Transaction> findByCardId(Long cardId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.type = :transactionType AND t.card.user.id = :userId")
    Page<Transaction> findByTypeAndUserId(@Param("transactionType") TransactionType transactionType,
                                          @Param("userId") Long UserId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.card.user.id = :userId")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
        "WHERE t.card.id = :cardId " +
        "AND t.type = 'WITHDRAWAL' " +
        "AND t.timestamp BETWEEN :startOfDay AND :endOfDay")
    BigDecimal getDailyWithdrawalSum(@Param("cardId") Long cardId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
        "WHERE t.card.id = :cardId " +
        "AND t.type = 'WITHDRAWAL' " +
        "AND t.timestamp BETWEEN :startOfMonth AND :endOfMonth")
    BigDecimal getMonthlyWithdrawalSum(@Param("cardId") Long cardId, @Param("startOfMonth") LocalDateTime startOfMonth, @Param("endOfMonth") LocalDateTime endOfMonth);
}
