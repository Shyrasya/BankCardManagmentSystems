package com.bank.cardmanagment.web.controller;

import com.bank.cardmanagment.domain.service.CardService;
import com.bank.cardmanagment.model.CardRequest;
import com.bank.cardmanagment.model.CardResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
        CardResponse response = cardService.createCard(cardRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-card/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId){
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/block-card/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> blockCard(@PathVariable Long cardId){
        cardService.blockCard(cardId);
        return ResponseEntity.ok("Карта c ID " + cardId + " заблокирована!");
    }

    @PatchMapping("/activate-card/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateCard(@PathVariable Long cardId){
        cardService.activateCard(cardId);
        return ResponseEntity.ok("Карта с ID " + cardId + " активирована!");
    }
}
