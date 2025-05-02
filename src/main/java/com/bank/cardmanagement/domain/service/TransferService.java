package com.bank.cardmanagement.domain.service;

import com.bank.cardmanagement.datasource.repository.CardRepository;
import com.bank.cardmanagement.datasource.repository.TransactionRepository;
import com.bank.cardmanagement.dto.request.TransferRequest;
import com.bank.cardmanagement.entity.Card;
import com.bank.cardmanagement.entity.Transaction;
import com.bank.cardmanagement.entity.TransactionType;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сервис для обработки переводов между картами.
 */
@Service
public class TransferService {

    /**
     * Репозиторий для работы с картами.
     */
    private final CardRepository cardRepository;

    /**
     * Репозиторий для работы с транзакциями.
     */
    private final TransactionRepository transactionRepository;

    /**
     * Сервис для валидации карт.
     */
    private final CardValidationService cardValidationService;

    /**
     * Конструктор для инициализации полей сервиса.
     *
     * @param cardRepository        репозиторий для работы с картами
     * @param transactionRepository репозиторий для работы с транзакциями
     * @param cardValidationService сервис для валидации карт
     */
    public TransferService(CardRepository cardRepository, TransactionRepository transactionRepository, CardValidationService cardValidationService) {
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
        this.cardValidationService = cardValidationService;
    }

    /**
     * Осуществляет перевод средств между картами.
     * Проверяет активность карт, наличие достаточных средств на исходной карте,
     * затем обновляет балансы карт и сохраняет транзакции.
     *
     * @param request объект с данными перевода, включая идентификаторы карт и сумму
     */
    @Transactional
    public void transferBetweenCards(@Valid @RequestBody TransferRequest request) {
        Card sourceCard = cardValidationService.isMyCard(request.getSourceCardId());
        Card destinationCard = cardValidationService.isMyCard(request.getDestinationCardId());
        cardValidationService.isActiveCard(sourceCard);
        cardValidationService.isActiveCard(destinationCard);
        BigDecimal amount = request.getAmount();
        cardValidationService.isEnoughMoney(sourceCard, amount);
        sourceCard.setBalance(sourceCard.getBalance().subtract(amount));
        destinationCard.setBalance(destinationCard.getBalance().add(amount));
        cardRepository.save(sourceCard);
        cardRepository.save(destinationCard);
        transactionRepository.save(new Transaction(TransactionType.TRANSFER, amount, "Перевод на карту ID " + destinationCard.getId(), LocalDateTime.now(), sourceCard));
        transactionRepository.save(new Transaction(TransactionType.TRANSFER, amount, "Получение перевода с карты ID " + sourceCard.getId(), LocalDateTime.now(), destinationCard));
    }
}
