package com.bank.cardmanagement.domain.service;

import com.bank.cardmanagement.datasource.repository.CardRepository;
import com.bank.cardmanagement.datasource.repository.TransactionRepository;
import com.bank.cardmanagement.dto.response.TransactionResponse;
import com.bank.cardmanagement.entity.Transaction;
import com.bank.cardmanagement.entity.TransactionType;
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
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardValidationService cardValidationService;

    private final Pageable pageable = PageRequest.of(0, 10);

    private final Transaction transaction = new Transaction(
            TransactionType.TRANSFER,
            BigDecimal.valueOf(100),
            "Test",
            LocalDateTime.now(),
            null
    );

    @Test
    void getAllTransactions_shouldReturnResponseWhenTypeAndCardIdExists() {
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        Mockito.when(transactionRepository.findByTypeAndCardId(TransactionType.TRANSFER, 1L, pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.getAllTransactions(TransactionType.TRANSFER, 1L, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("TRANSFER", result.getContent().get(0).getType());
        Mockito.verify(transactionRepository).findByTypeAndCardId(TransactionType.TRANSFER, 1L, pageable);

    }

    @Test
    void getAllTransactions_shouldReturnResponseWhenTypeExists() {
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        Mockito.when(transactionRepository.findByType(TransactionType.TRANSFER, pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.getAllTransactions(TransactionType.TRANSFER, null, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("TRANSFER", result.getContent().get(0).getType());
        Mockito.verify(transactionRepository).findByType(TransactionType.TRANSFER, pageable);
    }

    @Test
    void getAllTransactions_shouldReturnResponseWhenCardIdExists() {
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        Mockito.when(transactionRepository.findByCardId(1L, pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.getAllTransactions(null, 1L, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("TRANSFER", result.getContent().get(0).getType());
        Mockito.verify(transactionRepository).findByCardId(1L, pageable);
    }

    @Test
    void getAllTransactions_shouldReturnResponseWhenNoTypeAndCardId() {
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        Mockito.when(transactionRepository.findAll(pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.getAllTransactions(null, null, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("TRANSFER", result.getContent().get(0).getType());
        Mockito.verify(transactionRepository).findAll(pageable);
    }

    @Test
    void getAllMyTransactions_shouldReturnResponseWhenTypeAndCardIdExists() {
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        Long userId = 22L;
        Long cardId = 1L;
        Mockito.when(cardValidationService.getCurrentUserId()).thenReturn(userId);
        Mockito.when(cardRepository.existsByIdAndUserId(cardId, userId)).thenReturn(true);
        Mockito.when(transactionRepository.findByTypeAndCardId(TransactionType.TRANSFER, cardId, pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.getAllMyTransactions(TransactionType.TRANSFER, cardId, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("TRANSFER", result.getContent().get(0).getType());
        Mockito.verify(transactionRepository).findByTypeAndCardId(TransactionType.TRANSFER, cardId, pageable);
    }

    @Test
    void getAllMyTransactions_shouldReturnResponseWhenTypeExists() {
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        Long userId = 22L;
        Mockito.when(cardValidationService.getCurrentUserId()).thenReturn(userId);
        Mockito.when(transactionRepository.findByTypeAndUserId(TransactionType.TRANSFER, userId, pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.getAllMyTransactions(TransactionType.TRANSFER, null, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("TRANSFER", result.getContent().get(0).getType());
        Mockito.verify(transactionRepository).findByTypeAndUserId(TransactionType.TRANSFER, userId, pageable);
    }

    @Test
    void getAllMyTransactions_shouldReturnResponseWhenCardIdExists() {
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        Long userId = 22L;
        Long cardId = 1L;
        Mockito.when(cardValidationService.getCurrentUserId()).thenReturn(userId);
        Mockito.when(cardRepository.existsByIdAndUserId(cardId, userId)).thenReturn(true);
        Mockito.when(transactionRepository.findByCardId(cardId, pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.getAllMyTransactions(null, cardId, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("TRANSFER", result.getContent().get(0).getType());
        Mockito.verify(transactionRepository).findByCardId(cardId, pageable);
    }

    @Test
    void getAllMyTransactions_shouldReturnResponseWhenUserIdExists() {
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        Long userId = 22L;
        Mockito.when(cardValidationService.getCurrentUserId()).thenReturn(userId);
        Mockito.when(transactionRepository.findByUserId(userId, pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.getAllMyTransactions(null, null, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("TRANSFER", result.getContent().get(0).getType());
        Mockito.verify(transactionRepository).findByUserId(userId, pageable);
    }

    @Test
    void getAllMyTransactions_shouldThrowExceptionIfNotUserCard() {
        Long userId = 22L;
        Long cardId = 1L;
        Mockito.when(cardValidationService.getCurrentUserId()).thenReturn(userId);
        Mockito.when(cardRepository.existsByIdAndUserId(cardId, userId)).thenReturn(false);

        AccessDeniedException exception = Assertions.assertThrows(AccessDeniedException.class, () ->
                transactionService.getAllMyTransactions(null, cardId, pageable));

        Assertions.assertEquals("Вы не имеете доступа к данной карте!", exception.getMessage());
    }

}
