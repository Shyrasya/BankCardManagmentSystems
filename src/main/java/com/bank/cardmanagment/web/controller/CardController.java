package com.bank.cardmanagment.web.controller;

import com.bank.cardmanagment.domain.service.CardService;
import com.bank.cardmanagment.model.CardRequest;
import com.bank.cardmanagment.model.CardResponse;
import com.bank.cardmanagment.model.CardStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/card-managment/")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/create-card")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody(required = true) CardRequest cardRequest) {
        if (cardRequest.getUserId() <= 0){
            throw new IllegalArgumentException("ID пользователя не может быть отрицательным или нулевым!");
        }
        CardResponse response = cardService.createCard(cardRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-card/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCard(@PathVariable Long cardId){
        if (cardId <= 0) {
            throw new IllegalArgumentException("ID карты не может быть отрицательным или нулевым!");
        }
        cardService.deleteCard(cardId);
        return ResponseEntity.ok("Карта с ID " + cardId + " была успешно удалена!");
    }

    @PatchMapping("/block-card/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> blockCard(@PathVariable Long cardId){
        if (cardId <= 0) {
            throw new IllegalArgumentException("ID карты не может быть отрицательным или нулевым!");
        }
        cardService.blockCard(cardId);
        return ResponseEntity.ok("Карта c ID " + cardId + " заблокирована!");
    }

    @PatchMapping("/activate-card/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateCard(@PathVariable Long cardId){
        if (cardId <= 0) {
            throw new IllegalArgumentException("ID карты не может быть отрицательным или нулевым!");
        }
        cardService.activateCard(cardId);
        return ResponseEntity.ok("Карта с ID " + cardId + " активирована!");
    }

    @GetMapping("/get-cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size){
        CardStatus cardStatus = null;
        if (status != null) {
            try {
                cardStatus = CardStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Неверное значение статуса! Доступные значения: " + Arrays.toString(CardStatus.values()));
            }
        }
        if (userId != null && userId <= 0) {
            throw new IllegalArgumentException("ID пользователя не может быть отрицательным или нулевым!");
        }
        if (page <= 0) {
            throw new IllegalArgumentException("Номер страницы должен быть больше нуля!");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Размер страницы должен быть больше нуля!");
        }
        int correctedPage = page - 1;
        Pageable pageable = PageRequest.of(correctedPage, size, Sort.by("id").ascending());
        Page<CardResponse> cardResponses = cardService.getAllCards(cardStatus, userId, pageable);
        return ResponseEntity.ok(cardResponses);
    }
}
