package com.bank.cardmanagment.web.controller;


import com.bank.cardmanagment.domain.service.TransactionService;
import com.bank.cardmanagment.model.CardStatus;
import com.bank.cardmanagment.model.TransactionResponse;
import com.bank.cardmanagment.model.TransactionType;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
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
            @RequestParam(value = "cardId", required = false)
            @Min(value = 1, message = "ID карты должен быть положительным числом!") Long cardId,
            @RequestParam(value = "page", defaultValue = "1")
            @Min(value = 1, message = "Номер страницы должен быть больше нуля!") int page,
            @RequestParam(value = "size", defaultValue = "10")
            @Min(value = 1, message = "Размер страницы должен быть больше нуля!") int size){
        TransactionType transactionType = parseType(type);
        int correctPage = page - 1;
        Pageable pageable = PageRequest.of(correctPage, size, Sort.by("id").ascending());
        Page<TransactionResponse> transactionResponses = transactionService.getAllTransactions(transactionType, cardId, pageable);
        return ResponseEntity.ok(transactionResponses);
    }

    private TransactionType parseType(String type) {
        if (!StringUtils.hasText(type)) {
            return null;
        }
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неверное значение типа транзакций! Доступные значения: " + Arrays.toString(TransactionType.values()));
        }
    }

//    @GetMapping("/get-my-transactions")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<Page<TransactionResponse>> getAllMyTransaction(){
//
//    }
}
