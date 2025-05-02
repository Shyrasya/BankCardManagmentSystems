package com.bank.cardmanagement.datasource.repository;

import com.bank.cardmanagement.entity.Card;
import com.bank.cardmanagement.entity.Transaction;
import com.bank.cardmanagement.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Репозиторий для работы с сущностью {@link Transaction}.
 * Предоставляет методы для поиска, фильтрации и модификации данных транзакций.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Находит транзакции по типу транзакции и ID карты.
     *
     * @param transactionType тип транзакции.
     * @param cardId          идентификатор карты.
     * @param pageable        объект для пагинации.
     * @return {@link Page} с транзакциями, удовлетворяющими условиям поиска.
     */
    Page<Transaction> findByTypeAndCardId(TransactionType transactionType, Long cardId, Pageable pageable);

    /**
     * Находит транзакции по типу транзакции.
     *
     * @param transactionType тип транзакции.
     * @param pageable        объект для пагинации.
     * @return {@link Page} с транзакциями, удовлетворяющими условиям поиска.
     */
    Page<Transaction> findByType(TransactionType transactionType, Pageable pageable);

    /**
     * Находит транзакции по ID карты.
     *
     * @param cardId   идентификатор карты.
     * @param pageable объект для пагинации.
     * @return {@link Page} с транзакциями, связанных с указанной картой.
     */
    Page<Transaction> findByCardId(Long cardId, Pageable pageable);

    /**
     * Находит транзакции по типу транзакции и ID пользователя.
     *
     * @param transactionType тип транзакции.
     * @param userId          идентификатор пользователя.
     * @param pageable        объект для пагинации.
     * @return {@link Page} с транзакциями, удовлетворяющими условиям поиска.
     */
    @Query("SELECT t FROM Transaction t WHERE t.type = :transactionType AND t.card.user.id = :userId")
    Page<Transaction> findByTypeAndUserId(@Param("transactionType") TransactionType transactionType,
                                          @Param("userId") Long userId, Pageable pageable);

    /**
     * Находит транзакции по ID пользователя.
     *
     * @param userId   идентификатор пользователя.
     * @param pageable объект для пагинации.
     * @return {@link Page} с транзакциями, связанными с указанным пользователем.
     */
    @Query("SELECT t FROM Transaction t WHERE t.card.user.id = :userId")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Получает сумму снятых средств по карте за определенный день.
     *
     * @param cardId     идентификатор карты.
     * @param startOfDay начало дня.
     * @param endOfDay   конец дня.
     * @return сумма снятых средств.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.card.id = :cardId " +
            "AND t.type = 'WITHDRAWAL' " +
            "AND t.timestamp BETWEEN :startOfDay AND :endOfDay")
    BigDecimal getDailyWithdrawalSum(@Param("cardId") Long cardId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Получает сумму снятых средств по карте за определенный месяц.
     *
     * @param cardId       идентификатор карты.
     * @param startOfMonth начало месяца.
     * @param endOfMonth   конец месяца.
     * @return сумма снятых средств.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.card.id = :cardId " +
            "AND t.type = 'WITHDRAWAL' " +
            "AND t.timestamp BETWEEN :startOfMonth AND :endOfMonth")
    BigDecimal getMonthlyWithdrawalSum(@Param("cardId") Long cardId, @Param("startOfMonth") LocalDateTime startOfMonth, @Param("endOfMonth") LocalDateTime endOfMonth);

    /**
     * Удаляет транзакции по карте.
     *
     * @param card карта, для которой необходимо удалить транзакции.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Transaction t WHERE t.card = :card")
    void deleteByCard(@Param("card") Card card);
}
