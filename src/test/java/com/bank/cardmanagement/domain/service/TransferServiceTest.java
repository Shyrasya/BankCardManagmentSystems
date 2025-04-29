package com.bank.cardmanagement.domain.service;

import com.bank.cardmanagement.datasource.repository.CardRepository;
import com.bank.cardmanagement.datasource.repository.TransactionRepository;
import com.bank.cardmanagement.dto.request.TransferRequest;
import com.bank.cardmanagement.entity.Card;
import com.bank.cardmanagement.entity.CardStatus;
import com.bank.cardmanagement.entity.Transaction;
import com.bank.cardmanagement.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {
    @InjectMocks
    private TransferService transferService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardValidationService cardValidationService;

    @Test
    void transferBetweenCards_shouldBeSuccessful(){
        TransferRequest request = new TransferRequest();
        request.setSourceCardId(1L);
        request.setDestinationCardId(2L);
        request.setAmount(BigDecimal.valueOf(100));
        Card sourceCard = new Card();
        sourceCard.setId(1L);
        sourceCard.setBalance(BigDecimal.valueOf(200));
        sourceCard.setStatus(CardStatus.ACTIVE);
        Card destinationCard = new Card();
        destinationCard.setId(2L);
        destinationCard.setBalance(BigDecimal.valueOf(0));
        destinationCard.setStatus(CardStatus.ACTIVE);
        User user = new User();
        user.setId(5L);
        sourceCard.setUser(user);
        destinationCard.setUser(user);
        Mockito.when(cardValidationService.isMyCard(1L)).thenReturn(sourceCard);
        Mockito.when(cardValidationService.isMyCard(2L)).thenReturn(destinationCard);
        Mockito.doNothing().when(cardValidationService).isActiveCard(Mockito.any());
        Mockito.doNothing().when(cardValidationService).isEnoughMoney(sourceCard, request.getAmount());

        transferService.transferBetweenCards(request);

        Assertions.assertEquals(BigDecimal.valueOf(100), sourceCard.getBalance());
        Assertions.assertEquals(BigDecimal.valueOf(100), destinationCard.getBalance());
        Mockito.verify(cardRepository).save(sourceCard);
        Mockito.verify(cardRepository).save(destinationCard);
        Mockito.verify(transactionRepository, Mockito.times(2)).save(Mockito.any(Transaction.class));
    }
}
