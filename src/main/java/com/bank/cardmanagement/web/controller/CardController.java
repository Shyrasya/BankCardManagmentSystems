package com.bank.cardmanagement.web.controller;

import com.bank.cardmanagement.domain.service.CardService;
import com.bank.cardmanagement.dto.request.CardRequest;
import com.bank.cardmanagement.dto.response.CardResponse;
import com.bank.cardmanagement.entity.CardStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/card-management/")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/create-card")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest cardRequest) {
        CardResponse response = cardService.createCard(cardRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-card/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCard(@PathVariable("cardId") @NotNull @Min(1) Long cardId){
        cardService.deleteCard(cardId);
        return ResponseEntity.ok("Карта с ID " + cardId + " была успешно удалена!");
    }

    @PatchMapping("/block-card/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> blockCard(@PathVariable("cardId") @NotNull @Min(1) Long cardId){
        cardService.blockCard(cardId);
        return ResponseEntity.ok("Карта c ID " + cardId + " заблокирована!");
    }

    @PatchMapping("/activate-card/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateCard(@PathVariable("cardId") @NotNull @Min(1) Long cardId){
        cardService.activateCard(cardId);
        return ResponseEntity.ok("Карта с ID " + cardId + " активирована!");
    }

    @GetMapping("/get-cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "userId", required = false)
            @Min(value = 1, message = "ID пользователя должен быть положительным числом!") Long userId,
            @RequestParam(value = "page", defaultValue = "1")
            @Min(value = 1, message = "Номер страницы должен быть больше нуля!") int page,
            @RequestParam(value = "size", defaultValue = "10")
            @Min(value = 1, message = "Размер страницы должен быть больше нуля!") int size){
        CardStatus cardStatus = parseStatus(status);
        int correctedPage = page - 1;
        Pageable pageable = PageRequest.of(correctedPage, size, Sort.by("id").ascending());
        Page<CardResponse> cardResponses = cardService.getAllCards(cardStatus, userId, pageable);
        return ResponseEntity.ok(cardResponses);
    }

    @GetMapping("/get-my-cards")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<CardResponse>> getAllMyCards(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1")
            @Min(value = 1, message = "Номер страницы должен быть больше нуля!") int page,
            @RequestParam(value = "size", defaultValue = "10")
            @Min(value = 1, message = "Размер страницы должен быть больше нуля!") int size) {
        CardStatus cardStatus = parseStatus(status);
        int correctedPage = page - 1;
        Pageable pageable = PageRequest.of(correctedPage, size, Sort.by("id").ascending());
        Page<CardResponse> cardResponses = cardService.getAllMyCards(cardStatus, pageable);
        return ResponseEntity.ok(cardResponses);
    }

    private CardStatus parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        try {
            return CardStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неверное значение статуса карт! Доступные значения: " + Arrays.toString(CardStatus.values()));
        }
    }

    @PatchMapping("/block-my-card/{cardId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> blockMyCard(@PathVariable("cardId") @NotNull @Min(1) Long cardId){
        cardService.blockMyCard(cardId);
        return ResponseEntity.ok("Карта c ID " + cardId + " заблокирована!");
    }
}
