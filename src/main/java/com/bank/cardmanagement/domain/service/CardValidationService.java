package com.bank.cardmanagement.domain.service;

import com.bank.cardmanagement.datasource.repository.CardRepository;
import com.bank.cardmanagement.entity.Card;
import com.bank.cardmanagement.entity.CardStatus;
import com.bank.cardmanagement.exception.CardNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Сервис для валидации карт пользователя.
 * Обеспечивает проверки, такие как наличие карты, доступ к карте, активность карты и наличие достаточных средств.
 */
@Service
public class CardValidationService {
    /**
     * Репозиторий для работы с картами
     */
    private final CardRepository cardRepository;

    /**
     * Конструктор для инициализации сервиса валидации карт.
     *
     * @param cardRepository репозиторий для работы с картами.
     */
    public CardValidationService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * Проверяет, что карта принадлежит текущему пользователю.
     *
     * @param cardId идентификатор карты
     * @return карту, если она принадлежит текущему пользователю
     * @throws AccessDeniedException если карта не принадлежит текущему пользователю
     */
    public Card isMyCard(Long cardId) {
        Long userId = getCurrentUserId();
        Card card = findById(cardId);
        if (!card.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Нет доступа к данной карте!");
        }
        return card;
    }

    /**
     * Находит карту по идентификатору.
     *
     * @param cardId идентификатор карты
     * @return карту, если она найдена
     * @throws CardNotFoundException если карта с данным идентификатором не найдена
     */
    public Card findById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта с ID " + cardId + " не найдена!"));
    }

    /**
     * Получает идентификатор текущего пользователя.
     *
     * @return идентификатор текущего пользователя
     * @throws IllegalStateException если principal не является типом Long
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        } else {
            throw new IllegalStateException("Principal для UserId в JwtAuthentication не является типом Long!");
        }
    }

    /**
     * Проверяет, активна ли карта.
     *
     * @param card карта для проверки
     * @throws IllegalStateException если карта не активна
     */
    public void isActiveCard(Card card) {
        if (!card.getStatus().equals(CardStatus.ACTIVE)) {
            throw new IllegalStateException("Карта с ID " + card.getId() + " не активна!");
        }
    }

    /**
     * Проверяет, достаточно ли средств на карте для выполнения операции.
     *
     * @param card   карта для проверки
     * @param amount сумма, на которую нужно проверить наличие средств
     * @throws IllegalArgumentException если на карте недостаточно средств
     */
    public void isEnoughMoney(Card card, BigDecimal amount) {
        if (card.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Недостаточно средств на карте!");
        }
    }
}
