package com.bank.cardmanagment.web.controller;

import com.bank.cardmanagment.domain.service.CardService;
import com.bank.cardmanagment.model.CardRequest;
import com.bank.cardmanagment.model.CardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/card-managment/")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> createCard(@RequestBody CardRequest cardRequest) {
        CardResponse response = cardService.createCard(cardRequest);
        return ResponseEntity.ok(response);
    }
}
