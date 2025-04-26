package com.bank.cardmanagment.domain.service;
import com.bank.cardmanagment.datasource.repository.CardRepository;
import com.bank.cardmanagment.datasource.repository.UserRepository;
import com.bank.cardmanagment.exception.CardNotFoundException;
import com.bank.cardmanagment.exception.UserNotFoundException;
import com.bank.cardmanagment.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    private final EncryptionService encryptionService;

    public CardService(CardRepository cardRepository, UserRepository userRepository, EncryptionService encryptionService) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    public CardResponse createCard(CardRequest cardRequest){
        Long userId = cardRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

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
        Card saved = cardRepository.save(card);

        return new CardResponse(
                saved.getId(),
                maskedCardNumber,
                saved.getExpirationDate().toString(),
                saved.getStatus().name(),
                saved.getBalance());
    }

    private String generateCardNumber(){
        Random random = new Random();
        StringBuilder number = new StringBuilder("4000");
        for (int i = 0; i < 12; i++){
            number.append(random.nextInt(10));
        }
        return number.toString();
    }

    private String maskCardNumber(String number) {
        return number.replaceAll("(\\d{4})(\\d{8})(\\d{4})", "$1********$3");
    }

    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException("Карта с ID " + cardId + " не найдена!");
        }
        cardRepository.deleteById(cardId);
    }


    public void blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта с ID " + cardId + " не найдена!"));
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    public void activateCard(Long cardId){
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта с ID " + cardId + " не найдена!"));
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    public Page<CardResponse> getAllCards(CardStatus cardStatus, Long userId, Pageable pageable){
        if (cardStatus != null && userId != null){
            return cardRepository.findByStatusAndUserId(cardStatus, userId, pageable)
                    .map(this::convertToCardResponse);
        } else if (cardStatus != null){
            return cardRepository.findByStatus(cardStatus, pageable)
                    .map(this::convertToCardResponse);
        } else if (userId != null && userId > 0){
            return cardRepository.findByUserId(userId, pageable)
                    .map(this::convertToCardResponse);
        }
        return cardRepository.findAll(pageable)
                .map(this::convertToCardResponse);
    }

    private CardResponse convertToCardResponse(Card card){
        return new CardResponse(
                card.getId(),
                maskCardNumber(encryptionService.decrypt(card.getEncryptedCardNumber())),
                card.getExpirationDate().toString(),
                card.getStatus().name(),
                card.getBalance());
    }


}
