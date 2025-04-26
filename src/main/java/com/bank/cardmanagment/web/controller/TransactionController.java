package com.bank.cardmanagment.web.controller;


import com.bank.cardmanagment.domain.service.TransactionService;
import com.bank.cardmanagment.model.TransactionResponse;
import com.bank.cardmanagment.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/card-management/")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/get-transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TransactionResponse>> getAllTransaction(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "cardId", required = false) Long cardId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size){
        TransactionType transactionType = null;
        if (type != null) {
            try {
                transactionType = TransactionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Неверное значение типа транзакций! Доступные значения: " + Arrays.toString(TransactionType.values()));
            }
        }
        if (cardId != null && cardId <= 0){
            throw new IllegalArgumentException("ID карты не может быть отрицательным или нулевым!");
        }
        if (page <= 0){
            throw new IllegalArgumentException("Номер страницы должен быть больше нуля!");
        }
        if (size <= 0){
            throw new IllegalArgumentException("Размер страницы должен быть больше нуля!");
        }
        int correctPage = page - 1;
        Pageable pageable = PageRequest.of(correctPage, size, Sort.by("id").ascending());
        Page<TransactionResponse> transactionResponses = transactionService.getAllTransactions(transactionType, cardId, pageable);
        return ResponseEntity.ok(transactionResponses);
    }
}
