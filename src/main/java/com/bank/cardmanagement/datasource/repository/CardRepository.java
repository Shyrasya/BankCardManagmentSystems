package com.bank.cardmanagement.datasource.repository;

import com.bank.cardmanagement.entity.Card;
import com.bank.cardmanagement.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для работы с сущностью {@link Card}.
 * Предоставляет методы для поиска и модификации данных карт.
 */
public interface CardRepository extends JpaRepository<Card, Long> {

    /**
     * Проверяет, существует ли карта с указанным зашифрованным номером.
     *
     * @param encryptedCardNumber зашифрованный номер карты.
     * @return {@code true}, если карта с таким номером существует, {@code false} в противном случае.
     */
    boolean existsByEncryptedCardNumber(String encryptedCardNumber);

    /**
     * Находит карты по идентификатору пользователя с пагинацией.
     *
     * @param userId   идентификатор пользователя.
     * @param pageable объект для пагинации.
     * @return {@link Page} с картами, принадлежащими указанному пользователю.
     */
    Page<Card> findByUserId(Long userId, Pageable pageable);

    /**
     * Находит карты по статусу с пагинацией.
     *
     * @param status   статус карт.
     * @param pageable объект для пагинации.
     * @return {@link Page} с картами, имеющими указанный статус.
     */
    Page<Card> findByStatus(CardStatus status, Pageable pageable);

    /**
     * Находит карты по статусу и идентификатору пользователя с пагинацией.
     *
     * @param status   статус карт.
     * @param userId   идентификатор пользователя.
     * @param pageable объект для пагинации.
     * @return {@link Page} с картами, удовлетворяющими условиям поиска.
     */
    Page<Card> findByStatusAndUserId(CardStatus status, Long userId, Pageable pageable);

    /**
     * Проверяет, существует ли карта с указанным идентификатором и привязанным пользователем.
     *
     * @param id     идентификатор карты.
     * @param userId идентификатор пользователя.
     * @return {@code true}, если карта с таким ID принадлежит пользователю, {@code false} в противном случае.
     */
    boolean existsByIdAndUserId(Long id, Long userId);
}
