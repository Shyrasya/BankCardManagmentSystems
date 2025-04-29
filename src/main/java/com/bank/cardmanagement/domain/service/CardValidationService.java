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

@Service
public class CardValidationService {
    private final CardRepository cardRepository;


    public CardValidationService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public Card isMyCard(Long cardId){
        Long userId = getCurrentUserId();
        Card card = findById(cardId);
        if (!card.getUser().getId().equals(userId)){
            throw new AccessDeniedException("Нет доступа к данной карте!");
        }
        return card;
    }

    public Card findById(Long cardId){
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта с ID " + cardId + " не найдена!"));
    }

    public Long getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        } else {
            throw new IllegalStateException("Principal для UserId в JwtAuthentication не является типом Long!");
        }
    }

    public void isActiveCard(Card card){
        if (!card.getStatus().equals(CardStatus.ACTIVE)){
            throw new IllegalStateException("Карта с ID " + card.getId() + " не активна!");
        }
    }

    public void isEnoughMoney(Card card, BigDecimal amount){
        if (card.getBalance().compareTo(amount) < 0){
            throw new IllegalArgumentException("Недостаточно средств на карте!");
        }
    }
}
