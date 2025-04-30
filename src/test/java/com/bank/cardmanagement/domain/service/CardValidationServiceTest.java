package com.bank.cardmanagement.domain.service;

import com.bank.cardmanagement.datasource.repository.CardRepository;
import com.bank.cardmanagement.entity.Card;
import com.bank.cardmanagement.entity.CardStatus;
import com.bank.cardmanagement.entity.User;
import com.bank.cardmanagement.exception.CardNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;

import java.util.Optional;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class CardValidationServiceTest {

    @InjectMocks
    private CardValidationService cardValidationService;

    @Mock
    private CardRepository cardRepository;

    private void mockPrincipal(Long userId){
        Authentication authentication = mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    @Test
    void isMyCard_shouldReturnCheckedCard(){
        SecurityContextHolder.clearContext();
        Card card = new Card();
        User user = new User();
        user.setId(5L);
        card.setUser(user);
        Mockito.when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        mockPrincipal(5L);

        Card result = cardValidationService.isMyCard(10L);

        Assertions.assertEquals(5L, result.getUser().getId());
    }

    @Test
    void isMyCard_shouldThrowExceptionIfNotMyCard(){
        SecurityContextHolder.clearContext();
        Card card = new Card();
        User user = new User();
        user.setId(9L);
        card.setUser(user);
        Mockito.when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        mockPrincipal(5L);

        AccessDeniedException exception = Assertions.assertThrows(AccessDeniedException.class, () ->
                cardValidationService.isMyCard(10L));

        Assertions.assertEquals("Нет доступа к данной карте!", exception.getMessage());
    }

    @Test
    void findById_shouldReturnCard() {
        Card card = new Card();
        card.setId(1L);
        Mockito.when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        Card result = cardValidationService.findById(1L);

        Assertions.assertEquals(1L, result.getId());
    }

    @Test
    void findById_shouldThrowExceptionIfCardNotFound() {
        Mockito.when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        CardNotFoundException exception = Assertions.assertThrows(CardNotFoundException.class, () ->
                cardValidationService.findById(1L));

        Assertions.assertEquals("Карта с ID 1 не найдена!", exception.getMessage());
    }

    @Test
    void getCurrentUserId_shouldReturnUserId() {
        SecurityContextHolder.clearContext();
        mockPrincipal(7L);

        Long result = cardValidationService.getCurrentUserId();

        Assertions.assertEquals(7L, result);
    }

    @Test
    void getCurrentUserId_shouldThrowExceptionIfPrincipalIsNotLong() {
        SecurityContextHolder.clearContext();
        Authentication authentication = mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn("not a long");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () ->
                cardValidationService.getCurrentUserId());

        Assertions.assertEquals("Principal для UserId в JwtAuthentication не является типом Long!", exception.getMessage());
    }

    @Test
    void isActiveCard_shouldBeActive() {
        Card card = new Card();
        card.setStatus(CardStatus.ACTIVE);

        Assertions.assertDoesNotThrow(() -> cardValidationService.isActiveCard(card));
    }

    @Test
    void isActiveCard_shouldThrowExceptionIfCardIsBlocked() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.BLOCKED);

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () ->
                cardValidationService.isActiveCard(card));

        Assertions.assertEquals("Карта с ID 1 не активна!", exception.getMessage());
    }

    @Test
    void isEnoughMoney_shouldBeBalanceIsEnough() {
        Card card = new Card();
        card.setBalance(new BigDecimal("1000"));

        Assertions.assertDoesNotThrow(() -> cardValidationService.isEnoughMoney(card, new BigDecimal("500")));
    }

    @Test
    void isEnoughMoney_shouldThrowExceptionIsBalanceIsInsufficient() {
        Card card = new Card();
        card.setBalance(new BigDecimal("200"));

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                cardValidationService.isEnoughMoney(card, new BigDecimal("500")));

        Assertions.assertEquals("Недостаточно средств на карте!", exception.getMessage());
    }

}
