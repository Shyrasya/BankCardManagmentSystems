package com.bank.cardmanagement.domain.service;

import com.bank.cardmanagement.datasource.repository.CardRepository;
import com.bank.cardmanagement.datasource.repository.TransactionRepository;
import com.bank.cardmanagement.datasource.repository.UserRepository;
import com.bank.cardmanagement.dto.request.CardLimitRequest;
import com.bank.cardmanagement.dto.request.CardRequest;
import com.bank.cardmanagement.dto.request.WithdrawRequest;
import com.bank.cardmanagement.dto.response.CardResponse;
import com.bank.cardmanagement.entity.*;
import com.bank.cardmanagement.exception.CardNotFoundException;
import com.bank.cardmanagement.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Random;

/**
 * Сервис для работы с банковскими картами.
 * Обеспечивает операции, связанные с картами пользователя, включая создание карт, их обновление и обработку транзакций.
 */
@Service
public class CardService {

    /**
     * репозиторий для работы с картами.
     */
    private final CardRepository cardRepository;

    /**
     * Репозиторий для работы с пользователями.
     */
    private final UserRepository userRepository;

    /**
     * Репозиторий для работы с транзакциями.
     */
    private final TransactionRepository transactionRepository;

    /**
     * Сервис для шифрования данных
     */
    private final EncryptionService encryptionService;

    /**
     * Сервис для валидации карт
     */
    private final CardValidationService cardValidationService;

    /**
     * Конструктор для инициализации сервиса работы с картами.
     *
     * @param cardRepository        репозиторий для работы с картами.
     * @param userRepository        репозиторий для работы с пользователями.
     * @param transactionRepository репозиторий для работы с транзакциями.
     * @param encryptionService     сервис для шифрования данных.
     * @param cardValidationService сервис для валидации карт.
     */
    public CardService(CardRepository cardRepository, UserRepository userRepository, TransactionRepository transactionRepository, EncryptionService encryptionService, CardValidationService cardValidationService) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.encryptionService = encryptionService;
        this.cardValidationService = cardValidationService;
    }

    /**
     * Создаёт новую банковскую карту для пользователя.
     * Генерирует номер карты, шифрует его и сохраняет в базе данных.
     *
     * @param cardRequest запрос на создание карты, содержащий информацию о пользователе.
     * @return объект типа {@link CardResponse}, содержащий информацию о созданной карте.
     * @throws UserNotFoundException если пользователь с указанным ID не найден.
     */
    @Transactional
    public CardResponse createCard(CardRequest cardRequest) {
        Long userId = cardRequest.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        String rawCardNumber;
        String encryptedCardNumber;
        do {
            rawCardNumber = generateCardNumber();
            encryptedCardNumber = encryptionService.encrypt(rawCardNumber);
        } while (cardRepository.existsByEncryptedCardNumber(encryptedCardNumber));

        String maskedCardNumber = maskCardNumber(rawCardNumber);
        Card card = new Card();
        card.setEncryptedCardNumber(encryptedCardNumber);
        card.setUser(user);
        card.setExpirationDate(LocalDate.now().plusYears(3));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setDailyLimit(BigDecimal.ZERO);
        card.setMonthlyLimit(BigDecimal.ZERO);
        Card saved = cardRepository.save(card);

        return new CardResponse(
                saved.getId(),
                maskedCardNumber,
                saved.getExpirationDate().toString(),
                saved.getStatus().name(),
                saved.getBalance(),
                saved.getUser().getId());
    }

    /**
     * Генерирует случайный номер карты в формате "4000" + 12 случайных цифр.
     *
     * @return сгенерированный номер карты в виде строки.
     */
    private String generateCardNumber() {
        Random random = new Random();
        StringBuilder number = new StringBuilder("4000");
        for (int i = 0; i < 12; i++) {
            number.append(random.nextInt(10));
        }
        return number.toString();
    }

    /**
     * Маскирует номер карты, скрывая 8 средних цифр, оставляя только первые и последние 4.
     *
     * @param number номер карты, который нужно замаскировать.
     * @return строка с замаскированным номером карты.
     */
    private String maskCardNumber(String number) {
        return number.replaceAll("(\\d{4})(\\d{8})(\\d{4})", "$1********$3");
    }

    /**
     * Удаляет банковскую карту по её ID.
     *
     * @param cardId ID карты, которую нужно удалить.
     * @throws CardNotFoundException если карта с указанным ID не найдена.
     */
    @Transactional
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException("Карта с ID " + cardId + " не найдена!");
        }
        cardRepository.deleteById(cardId);
    }

    /**
     * Блокирует карту по её ID.
     *
     * @param cardId ID карты, которую нужно заблокировать.
     * @throws CardNotFoundException если карта с указанным ID не найдена.
     * @throws IllegalStateException если карта не активна.
     */
    @Transactional
    public void blockCard(Long cardId) {
        Card card = cardValidationService.findById(cardId);
        blockCardInternal(card);
    }

    /**
     * Блокирует карту текущего пользователя по её ID.
     *
     * @param cardId ID карты, которую нужно заблокировать.
     * @throws CardNotFoundException если карта с указанным ID не найдена.
     * @throws IllegalStateException если карта не активна.
     */
    @Transactional
    public void blockMyCard(Long cardId) {
        Card card = cardValidationService.isMyCard(cardId);
        blockCardInternal(card);
    }

    /**
     * Внутренний метод для блокировки карты.
     *
     * @param card карта, которую нужно заблокировать.
     * @throws IllegalStateException если карта не активна.
     */
    private void blockCardInternal(Card card) {
        if (!card.getStatus().equals(CardStatus.ACTIVE)) {
            throw new IllegalStateException("Карта с ID " + card.getId() + " не является активной!");
        }
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    /**
     * Активирует карту по её ID.
     *
     * @param cardId ID карты, которую нужно активировать.
     * @throws CardNotFoundException если карта с указанным ID не найдена.
     */
    @Transactional
    public void activateCard(Long cardId) {
        Card card = cardValidationService.findById(cardId);
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    /**
     * Получает все карты с возможностью фильтрации по статусу и ID пользователя.
     *
     * @param cardStatus статус карты, по которому нужно фильтровать.
     * @param userId     ID пользователя, по которому нужно фильтровать.
     * @param pageable   объект, содержащий параметры постраничной навигации.
     * @return страница объектов {@link CardResponse}, содержащих информацию о картах.
     */
    public Page<CardResponse> getAllCards(CardStatus cardStatus, Long userId, Pageable pageable) {
        if (cardStatus != null && userId != null) {
            return cardRepository.findByStatusAndUserId(cardStatus, userId, pageable)
                    .map(this::convertToCardResponse);
        } else if (cardStatus != null) {
            return cardRepository.findByStatus(cardStatus, pageable)
                    .map(this::convertToCardResponse);
        } else if (userId != null) {
            return cardRepository.findByUserId(userId, pageable)
                    .map(this::convertToCardResponse);
        }
        return cardRepository.findAll(pageable)
                .map(this::convertToCardResponse);
    }

    /**
     * Получает все карты текущего пользователя с возможностью фильтрации по статусу.
     *
     * @param cardStatus статус карты, по которому нужно фильтровать.
     * @param pageable   объект, содержащий параметры постраничной навигации.
     * @return страница объектов {@link CardResponse}, содержащих информацию о картах.
     */
    public Page<CardResponse> getAllMyCards(CardStatus cardStatus, Pageable pageable) {
        Long userId = cardValidationService.getCurrentUserId();
        if (cardStatus != null) {
            return cardRepository.findByStatusAndUserId(cardStatus, userId, pageable)
                    .map(this::convertToCardResponse);
        }
        return cardRepository.findByUserId(userId, pageable)
                .map(this::convertToCardResponse);
    }

    /**
     * Преобразует объект типа {@link Card} в объект типа {@link CardResponse}.
     *
     * @param card карта, которую нужно преобразовать.
     * @return объект {@link CardResponse}, содержащий информацию о карте.
     */
    private CardResponse convertToCardResponse(Card card) {
        return new CardResponse(
                card.getId(),
                maskCardNumber(encryptionService.decrypt(card.getEncryptedCardNumber())),
                card.getExpirationDate().toString(),
                card.getStatus().name(),
                card.getBalance(),
                card.getUser().getId());
    }

    /**
     * Устанавливает лимиты для карты (ежедневный и/или месячный).
     *
     * @param cardId  ID карты, для которой нужно установить лимиты.
     * @param request объект с запросом на установку лимитов.
     * @throws CardNotFoundException    если карта с указанным ID не найдена.
     * @throws IllegalArgumentException если не указано ни одного лимита.
     */
    @Transactional
    public void setCardLimits(Long cardId, CardLimitRequest request) {
        Card card = cardValidationService.findById(cardId);
        if (request.getDailyLimit() == null && request.getMonthlyLimit() == null) {
            throw new IllegalArgumentException("Не указано ни одного лимита!");
        }
        if (request.getDailyLimit() != null) {
            card.setDailyLimit(request.getDailyLimit());
        }
        if (request.getMonthlyLimit() != null) {
            card.setMonthlyLimit(request.getMonthlyLimit());
        }
        cardRepository.save(card);
    }

    /**
     * Снимает наличные с карты, проверяя наличие средств, лимиты и доступность операции.
     *
     * @param cardId  ID карты, с которой нужно снять деньги.
     * @param request запрос на снятие наличных, содержащий сумму и описание.
     * @throws CardNotFoundException    если карта с указанным ID не найдена.
     * @throws IllegalArgumentException если на карте недостаточно средств или превышены лимиты.
     * @throws IllegalStateException    если карта не активна.
     */
    @Transactional
    public void cashWithdraw(Long cardId, WithdrawRequest request) {
        Card card = cardValidationService.isMyCard(cardId);
        cardValidationService.isActiveCard(card);
        BigDecimal amount = request.getAmount();
        cardValidationService.isEnoughMoney(card, amount);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        BigDecimal dailySpent = transactionRepository.getDailyWithdrawalSum(cardId, startOfDay, endOfDay);
        BigDecimal monthlySpent = transactionRepository.getMonthlyWithdrawalSum(cardId, startOfMonth, endOfMonth);
        BigDecimal dailyLimit = card.getDailyLimit();
        BigDecimal monthlyLimit = card.getMonthlyLimit();

        if (dailyLimit != null && dailySpent.add(amount).compareTo(dailyLimit) > 0) {
            throw new IllegalArgumentException("Превышен дневной лимит снятия наличных! Операция отклонена!");
        }
        if (monthlyLimit != null && monthlySpent.add(amount).compareTo(monthlyLimit) > 0) {
            throw new IllegalArgumentException("Превышен месячный лимит снятия наличных! Операция отклонена!");
        }
        card.setBalance(card.getBalance().subtract(amount));
        cardRepository.save(card);
        Transaction transaction = new Transaction(TransactionType.WITHDRAWAL, amount, request.getDescription(), LocalDateTime.now(), card);
        transactionRepository.save(transaction);
    }
}
