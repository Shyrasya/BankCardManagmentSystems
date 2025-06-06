package com.bank.cardmanagement.web.controller;

import com.bank.cardmanagement.domain.service.TransactionService;
import com.bank.cardmanagement.dto.response.TransactionResponse;
import com.bank.cardmanagement.entity.TransactionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * Контроллер для управления транзакциями.
 * Предоставляет эндпоинты для получения всех транзакций (администратором)
 * и транзакций конкретного пользователя.
 */
@RestController
@RequestMapping("/card-management/")
public class TransactionController {

    /**
     * Сервис для работы с транзакциями.
     */
    private final TransactionService transactionService;

    /**
     * Конструктор контроллера.
     *
     * @param transactionService сервис для обработки транзакций
     */
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Получить все транзакции (доступно только администратору).
     *
     * @param type   тип транзакции (опционально)
     * @param cardId ID карты (опционально)
     * @param page   номер страницы (по умолчанию 1)
     * @param size   размер страницы (по умолчанию 10)
     * @return страница с транзакциями
     */
    @GetMapping("/get-transactions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<TransactionResponse>> getAllTransaction(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "cardId", required = false)
            @Min(value = 1, message = "ID карты должен быть положительным числом!") Long cardId,
            @RequestParam(value = "page", defaultValue = "1")
            @Min(value = 1, message = "Номер страницы должен быть больше нуля!") int page,
            @RequestParam(value = "size", defaultValue = "10")
            @Min(value = 1, message = "Размер страницы должен быть больше нуля!") int size) {
        return ResponseEntity.ok(getTransactions(type, cardId, page, size, true));
    }

    /**
     * Получить транзакции текущего пользователя.
     *
     * @param type   тип транзакции (опционально)
     * @param cardId ID карты (опционально)
     * @param page   номер страницы (по умолчанию 1)
     * @param size   размер страницы (по умолчанию 10)
     * @return страница с транзакциями пользователя
     */
    @GetMapping("/get-my-transactions")
    @PreAuthorize("hasRole('USER')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<TransactionResponse>> getAllMyTransaction(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "cardId", required = false)
            @Min(value = 1, message = "ID карты должен быть положительным числом!") Long cardId,
            @RequestParam(value = "page", defaultValue = "1")
            @Min(value = 1, message = "Номер страницы должен быть больше нуля!") int page,
            @RequestParam(value = "size", defaultValue = "10")
            @Min(value = 1, message = "Размер страницы должен быть больше нуля!") int size) {
        return ResponseEntity.ok(getTransactions(type, cardId, page, size, false));
    }

    /**
     * Внутренний метод для получения транзакций.
     *
     * @param type    тип транзакции
     * @param cardId  ID карты
     * @param page    номер страницы (начиная с 0)
     * @param size    размер страницы
     * @param isAdmin флаг доступа администратора
     * @return страница с транзакциями
     */
    private Page<TransactionResponse> getTransactions(String type, Long cardId, int page, int size, boolean isAdmin) {
        TransactionType transactionType = parseType(type);
        int correctPage = page - 1;
        Pageable pageable = PageRequest.of(correctPage, size, Sort.by("id").ascending());
        if (isAdmin) {
            return transactionService.getAllTransactions(transactionType, cardId, pageable);
        }
        return transactionService.getAllMyTransactions(transactionType, cardId, pageable);
    }

    /**
     * Парсит строку в enum TransactionType.
     *
     * @param type строка с типом
     * @return тип транзакции или null
     * @throws IllegalArgumentException если тип некорректный
     */
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
}
