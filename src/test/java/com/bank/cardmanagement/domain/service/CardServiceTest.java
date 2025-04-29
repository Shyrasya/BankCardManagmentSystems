package com.bank.cardmanagement.domain.service;

import com.bank.cardmanagement.datasource.repository.CardRepository;
import com.bank.cardmanagement.datasource.repository.TransactionRepository;
import com.bank.cardmanagement.datasource.repository.UserRepository;
import com.bank.cardmanagement.dto.request.CardLimitRequest;
import com.bank.cardmanagement.dto.request.CardRequest;
import com.bank.cardmanagement.dto.request.WithdrawRequest;
import com.bank.cardmanagement.dto.response.CardResponse;
import com.bank.cardmanagement.entity.Card;
import com.bank.cardmanagement.entity.CardStatus;
import com.bank.cardmanagement.entity.Transaction;
import com.bank.cardmanagement.entity.User;
import com.bank.cardmanagement.exception.CardNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {
    @InjectMocks
    private CardService cardService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private CardValidationService cardValidationService;

    @Test
    void createCard_shouldCreate() {
        CardRequest request = new CardRequest();
        request.setUserId(1L);
        User user = new User();
        user.setId(1L);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        String encryptedCardNumber = "encrypted123";
        Mockito.when(encryptionService.encrypt(Mockito.anyString())).thenReturn(encryptedCardNumber);
        Mockito.when(cardRepository.existsByEncryptedCardNumber(encryptedCardNumber)).thenReturn(false);
        Card savedCard = new Card();
        savedCard.setId(1L);
        savedCard.setEncryptedCardNumber(encryptedCardNumber);
        savedCard.setUser(user);
        savedCard.setExpirationDate(LocalDate.now().plusYears(3));
        savedCard.setStatus(CardStatus.ACTIVE);
        savedCard.setBalance(BigDecimal.ZERO);
        Mockito.when(cardRepository.save(Mockito.any(Card.class))).thenReturn(savedCard);

        CardResponse response = cardService.createCard(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(1L, response.getId());
        Assertions.assertEquals(CardStatus.ACTIVE.name(), response.getStatus());
        Assertions.assertEquals(BigDecimal.ZERO, response.getBalance());
    }

    @Test
    void deleteCard_shouldDelete() {
        Mockito.when(cardRepository.existsById(1L)).thenReturn(true);

        cardService.deleteCard(1L);

        Mockito.verify(cardRepository).deleteById(1L);
    }

    @Test
    void deleteCard_shouldThrowExceptionIfCardNotExists() {
        Mockito.when(cardRepository.existsById(1L)).thenReturn(false);

        CardNotFoundException exception = Assertions.assertThrows(CardNotFoundException.class, () ->
                cardService.deleteCard(1L));

        Assertions.assertEquals("Карта с ID 1 не найдена!", exception.getMessage());
    }

    @Test
    void blockCard_shouldBlockActiveCard() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.ACTIVE);
        Mockito.when(cardValidationService.findById(1L)).thenReturn(card);

        cardService.blockCard(1L);

        Assertions.assertEquals(CardStatus.BLOCKED, card.getStatus());
        Mockito.verify(cardRepository).save(card);
    }

    @Test
    void blockCard_shouldThrowExceptionIfCardNotActive() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.BLOCKED);
        Mockito.when(cardValidationService.findById(1L)).thenReturn(card);

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () ->
                cardService.blockCard(1L));

        Assertions.assertEquals("Карта с ID 1 не является активной!", exception.getMessage());
    }

    @Test
    void blockMyCard_shouldBlockActiveOwnCard() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.ACTIVE);
        User user = new User();
        user.setId(3L);
        card.setUser(user);
        Mockito.when(cardValidationService.isMyCard(1L)).thenReturn(card);

        cardService.blockMyCard(1L);

        Assertions.assertEquals(CardStatus.BLOCKED, card.getStatus());
        Mockito.verify(cardRepository).save(card);
    }

    @Test
    void blockMyCard_shouldThrowExceptionIfCardNotActive() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.BLOCKED);
        User user = new User();
        user.setId(3L);
        card.setUser(user);
        Mockito.when(cardValidationService.isMyCard(1L)).thenReturn(card);

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () ->
                cardService.blockMyCard(1L));

        Assertions.assertEquals("Карта с ID 1 не является активной!", exception.getMessage());
    }

    @Test
    void activateCard_shouldActivate() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.BLOCKED);
        Mockito.when(cardValidationService.findById(1L)).thenReturn(card);

        cardService.activateCard(1L);

        Assertions.assertEquals(CardStatus.ACTIVE, card.getStatus());
        Mockito.verify(cardRepository).save(card);
    }

    @Test
    void activateCard_shouldThrowExceptionIfCardNotExists() {
        Mockito.when(cardValidationService.findById(1L))
                .thenThrow(new CardNotFoundException("Карта с ID 1 не найдена!"));

        CardNotFoundException exception = Assertions.assertThrows(CardNotFoundException.class, () ->
                cardService.activateCard(1L));

        Assertions.assertEquals("Карта с ID 1 не найдена!", exception.getMessage());
    }

    @Test
    void getAllCards_shouldReturnCardsByStatusAndUserId() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.ACTIVE);
        card.setEncryptedCardNumber("1234567812345678");
        card.setExpirationDate(LocalDate.now().plusYears(3));
        User user = new User();
        user.setId(3L);
        card.setUser(user);
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        Pageable pageable = PageRequest.of(0, 10);
        Mockito.when(cardRepository.findByStatusAndUserId(CardStatus.ACTIVE, 1L, pageable))
                .thenReturn(cardPage);
        Mockito.when(encryptionService.decrypt("1234567812345678"))
                .thenReturn("1234567812345678");

        Page<CardResponse> result = cardService.getAllCards(CardStatus.ACTIVE, 1L, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(CardStatus.ACTIVE.name(), result.getContent().get(0).getStatus());
        Assertions.assertEquals(3L, result.getContent().get(0).getUserId());
    }

    @Test
    void getAllCards_shouldReturnCardsByStatus() {
        Card card = new Card();
        card.setId(3L);
        User user = new User();
        user.setId(35L);
        card.setUser(user);
        card.setStatus(CardStatus.BLOCKED);
        card.setEncryptedCardNumber("1234567812345678");
        card.setExpirationDate(LocalDate.now().plusYears(3));
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        Pageable pageable = PageRequest.of(0, 10);
        Mockito.when(cardRepository.findByStatus(CardStatus.BLOCKED, pageable))
                .thenReturn(cardPage);
        Mockito.when(encryptionService.decrypt("1234567812345678"))
                .thenReturn("1234567812345678");

        Page<CardResponse> result = cardService.getAllCards(CardStatus.BLOCKED, null, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(CardStatus.BLOCKED.name(), result.getContent().get(0).getStatus());
    }

    @Test
    void getAllCards_shouldReturnCardsByUserId() {
        Card card = new Card();
        card.setId(3L);
        card.setStatus(CardStatus.ACTIVE);
        card.setEncryptedCardNumber("1234567812345678");
        card.setExpirationDate(LocalDate.now().plusYears(3));
        User user = new User();
        user.setId(5L);
        card.setUser(user);
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        Pageable pageable = PageRequest.of(0, 10);
        Mockito.when(cardRepository.findByUserId(5L, pageable))
                .thenReturn(cardPage);
        Mockito.when(encryptionService.decrypt("1234567812345678"))
                .thenReturn("1234567812345678");

        Page<CardResponse> result = cardService.getAllCards(null, 5L, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(5L, result.getContent().get(0).getUserId());
    }

    @Test
    void getAllCards_shouldReturnAllCards() {
        Card card = new Card();
        card.setId(3L);
        card.setStatus(CardStatus.ACTIVE);
        card.setEncryptedCardNumber("1234567812345678");
        card.setExpirationDate(LocalDate.now().plusYears(3));
        User user = new User();
        user.setId(7L);
        card.setUser(user);
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        Pageable pageable = PageRequest.of(0, 10);
        Mockito.when(cardRepository.findAll(pageable))
                .thenReturn(cardPage);
        Mockito.when(encryptionService.decrypt("1234567812345678"))
                .thenReturn("1234567812345678");

        Page<CardResponse> result = cardService.getAllCards(null, null, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(3L, result.getContent().get(0).getId());
    }

    @Test
    void getAllMyCards_shouldReturnCardsByStatusAndUserId() {
        Card card = new Card();
        card.setId(5L);
        card.setStatus(CardStatus.ACTIVE);
        card.setEncryptedCardNumber("1234567812345678");
        card.setExpirationDate(LocalDate.now().plusYears(2));
        User user = new User();
        user.setId(8L);
        card.setUser(user);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        Mockito.when(cardValidationService.getCurrentUserId()).thenReturn(8L);
        Mockito.when(cardRepository.findByStatusAndUserId(CardStatus.ACTIVE, 8L, pageable))
                .thenReturn(cardPage);
        Mockito.when(encryptionService.decrypt("1234567812345678"))
                .thenReturn("1234567812345678");

        Page<CardResponse> result = cardService.getAllMyCards(CardStatus.ACTIVE, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(CardStatus.ACTIVE.name(), result.getContent().get(0).getStatus());
        Assertions.assertEquals(8L, result.getContent().get(0).getUserId());
        Assertions.assertEquals(5L, result.getContent().get(0).getId());
    }

    @Test
    void getAllMyCards_shouldReturnCardsByUserId() {
        Card card = new Card();
        card.setId(9L);
        card.setStatus(CardStatus.ACTIVE);
        card.setEncryptedCardNumber("1234567812345678");
        card.setExpirationDate(LocalDate.now().plusYears(2));
        User user = new User();
        user.setId(13L);
        card.setUser(user);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        Mockito.when(cardValidationService.getCurrentUserId()).thenReturn(13L);
        Mockito.when(cardRepository.findByUserId(13L, pageable))
                .thenReturn(cardPage);
        Mockito.when(encryptionService.decrypt("1234567812345678"))
                .thenReturn("1234567812345678");

        Page<CardResponse> result = cardService.getAllMyCards(null, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(13L, result.getContent().get(0).getUserId());
        Assertions.assertEquals(9L, result.getContent().get(0).getId());
    }

    @Test
    void setCardLimits_shouldSetBothLimit() {
        Card card = new Card();
        card.setId(4L);
        Mockito.when(cardValidationService.findById(4L)).thenReturn(card);
        CardLimitRequest request = new CardLimitRequest();
        request.setDailyLimit(BigDecimal.valueOf(1000));
        request.setMonthlyLimit(BigDecimal.valueOf(20000));

        cardService.setCardLimits(4L, request);

        Assertions.assertEquals(BigDecimal.valueOf(1000), card.getDailyLimit());
        Assertions.assertEquals(BigDecimal.valueOf(20000), card.getMonthlyLimit());
        Mockito.verify(cardRepository).save(card);
    }

    @Test
    void setCardLimits_shouldSetDailyLimit() {
        Card card = new Card();
        card.setId(7L);
        Mockito.when(cardValidationService.findById(7L)).thenReturn(card);
        CardLimitRequest request = new CardLimitRequest();
        request.setDailyLimit(BigDecimal.valueOf(7000));

        cardService.setCardLimits(7L, request);

        Assertions.assertEquals(BigDecimal.valueOf(7000), card.getDailyLimit());
        Mockito.verify(cardRepository).save(card);
    }

    @Test
    void setCardLimits_shouldSetMonthlyLimit() {
        Card card = new Card();
        card.setId(6L);
        Mockito.when(cardValidationService.findById(6L)).thenReturn(card);
        CardLimitRequest request = new CardLimitRequest();
        request.setMonthlyLimit(BigDecimal.valueOf(60000));

        cardService.setCardLimits(6L, request);

        Assertions.assertEquals(BigDecimal.valueOf(60000), card.getMonthlyLimit());
        Mockito.verify(cardRepository).save(card);
    }

    @Test
    void setCardLimits_shouldThrowExceptionIfNoLimits() {
        Card card = new Card();
        card.setId(5L);
        Mockito.when(cardValidationService.findById(5L)).thenReturn(card);
        CardLimitRequest request = new CardLimitRequest();

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                cardService.setCardLimits(5L, request));

        Assertions.assertEquals("Не указано ни одного лимита!", exception.getMessage());
        Mockito.verify(cardRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void cashWithdraw_shouldPassAllLimit() {
        Card card = new Card();
        card.setId(2L);
        card.setBalance(BigDecimal.valueOf(1000));
        card.setDailyLimit(BigDecimal.valueOf(500));
        card.setDailyLimit(BigDecimal.valueOf(1500));
        card.setStatus(CardStatus.ACTIVE);
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(BigDecimal.valueOf(200));
        request.setDescription("ATM 584 Withdraw");
        Mockito.when(cardValidationService.isMyCard(2L)).thenReturn(card);
        Mockito.doNothing().when(cardValidationService).isActiveCard(card);
        Mockito.doNothing().when(cardValidationService).isEnoughMoney(card, BigDecimal.valueOf(200));
        Mockito.when(transactionRepository.getDailyWithdrawalSum(Mockito.eq(2L), Mockito.any(), Mockito.any()))
                .thenReturn(BigDecimal.valueOf(100));
        Mockito.when(transactionRepository.getMonthlyWithdrawalSum(Mockito.eq(2L), Mockito.any(), Mockito.any()))
                .thenReturn(BigDecimal.valueOf(200));

        cardService.cashWithdraw(2L, request);

        Assertions.assertEquals(BigDecimal.valueOf(800), card.getBalance());
        Mockito.verify(cardRepository).save(card);
        Mockito.verify(transactionRepository).save(Mockito.any(Transaction.class));
    }

    @Test
    void cashWithdraw_shouldThrowExceptionIfDailyLimitExceeded() {
        Card card = new Card();
        card.setId(7L);
        card.setBalance(BigDecimal.valueOf(1000));
        card.setDailyLimit(BigDecimal.valueOf(300));
        card.setMonthlyLimit(BigDecimal.valueOf(1500));
        card.setStatus(CardStatus.ACTIVE);

        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(BigDecimal.valueOf(250));
        request.setDescription("ATM 585 Withdraw");

        Mockito.when(cardValidationService.isMyCard(7L)).thenReturn(card);
        Mockito.doNothing().when(cardValidationService).isActiveCard(card);
        Mockito.doNothing().when(cardValidationService).isEnoughMoney(card, BigDecimal.valueOf(250));
        Mockito.when(transactionRepository.getDailyWithdrawalSum(Mockito.eq(7L), Mockito.any(), Mockito.any()))
                .thenReturn(BigDecimal.valueOf(100));
        Mockito.when(transactionRepository.getMonthlyWithdrawalSum(Mockito.eq(7L), Mockito.any(), Mockito.any()))
                .thenReturn(BigDecimal.valueOf(200));

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> cardService.cashWithdraw(7L, request));

        Assertions.assertEquals("Превышен дневной лимит снятия наличных! Операция отклонена!", ex.getMessage());
        Mockito.verify(cardRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void cashWithdraw_shouldThrowExceptionIfMonthlyLimitExceeded() {
        Card card = new Card();
        card.setId(16L);
        card.setBalance(BigDecimal.valueOf(1000));
        card.setDailyLimit(BigDecimal.valueOf(500));
        card.setMonthlyLimit(BigDecimal.valueOf(700));
        card.setStatus(CardStatus.ACTIVE);

        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(BigDecimal.valueOf(300));
        request.setDescription("ATM 586 Withdraw");

        Mockito.when(cardValidationService.isMyCard(16L)).thenReturn(card);
        Mockito.doNothing().when(cardValidationService).isActiveCard(card);
        Mockito.doNothing().when(cardValidationService).isEnoughMoney(card, BigDecimal.valueOf(300));
        Mockito.when(transactionRepository.getDailyWithdrawalSum(Mockito.eq(16L), Mockito.any(), Mockito.any()))
                .thenReturn(BigDecimal.valueOf(100));
        Mockito.when(transactionRepository.getMonthlyWithdrawalSum(Mockito.eq(16L), Mockito.any(), Mockito.any()))
                .thenReturn(BigDecimal.valueOf(500));

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> cardService.cashWithdraw(16L, request));

        Assertions.assertEquals("Превышен месячный лимит снятия наличных! Операция отклонена!", ex.getMessage());
        Mockito.verify(cardRepository, Mockito.never()).save(Mockito.any());
    }
}
