package com.bank.cardmanagement.web.controller;

import com.bank.cardmanagement.domain.service.CardService;
import com.bank.cardmanagement.domain.service.TransferService;
import com.bank.cardmanagement.dto.request.CardLimitRequest;
import com.bank.cardmanagement.dto.request.CardRequest;
import com.bank.cardmanagement.dto.request.TransferRequest;
import com.bank.cardmanagement.dto.request.WithdrawRequest;
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

/**
 * Контроллер для управления банковскими картами.
 * Предоставляет эндпоинты для создания, удаления, блокировки и активации карт.
 */
@RestController
@RequestMapping("/card-management/")
public class CardController {

    /**
     * Сервис для работы с картами.
     */
    private final CardService cardService;

    /**
     * Сервис для перевода средств.
     */
    private final TransferService transferService;

    /**
     * Конструктор контроллера.
     *
     * @param cardService     сервис для работы с картами
     * @param transferService сервис для перевода средств
     */
    public CardController(CardService cardService, TransferService transferService) {
        this.cardService = cardService;
        this.transferService = transferService;
    }

    /**
     * Создать новую банковскую карту.
     * Доступно только администраторам.
     *
     * @param cardRequest объект с данными карты
     * @return объект с данными созданной карты
     */
    @PostMapping("/create-card")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest cardRequest) {
        CardResponse response = cardService.createCard(cardRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Удалить банковскую карту.
     * Доступно только администраторам.
     *
     * @param cardId ID карты, которую необходимо удалить
     * @return сообщение об успешном удалении карты
     */
    @DeleteMapping("/delete-card/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCard(@PathVariable("cardId") @NotNull @Min(1) Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok("Карта с ID " + cardId + " была успешно удалена!");
    }

    /**
     * Заблокировать банковскую карту.
     * Доступно только администраторам.
     *
     * @param cardId ID карты, которую необходимо заблокировать
     * @return сообщение о блокировке карты
     */
    @PatchMapping("/block-card/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> blockCard(@PathVariable("cardId") @NotNull @Min(1) Long cardId) {
        cardService.blockCard(cardId);
        return ResponseEntity.ok("Карта c ID " + cardId + " заблокирована!");
    }

    /**
     * Активировать банковскую карту.
     * Доступно только администраторам.
     *
     * @param cardId ID карты, которую необходимо активировать
     * @return сообщение о активации карты
     */
    @PatchMapping("/activate-card/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateCard(@PathVariable("cardId") @NotNull @Min(1) Long cardId) {
        cardService.activateCard(cardId);
        return ResponseEntity.ok("Карта с ID " + cardId + " активирована!");
    }

    /**
     * Получить все карты (для администратора).
     * Доступно только администраторам.
     *
     * @param status статус карт (необязательный параметр)
     * @param userId ID пользователя, чьи карты нужно получить (необязательный параметр)
     * @param page   номер страницы (по умолчанию 1)
     * @param size   размер страницы (по умолчанию 10)
     * @return список карт, соответствующих фильтрам
     */
    @GetMapping("/get-cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "userId", required = false)
            @Min(value = 1, message = "ID пользователя должен быть положительным числом!") Long userId,
            @RequestParam(value = "page", defaultValue = "1")
            @Min(value = 1, message = "Номер страницы должен быть больше нуля!") int page,
            @RequestParam(value = "size", defaultValue = "10")
            @Min(value = 1, message = "Размер страницы должен быть больше нуля!") int size) {
        CardStatus cardStatus = parseStatus(status);
        int correctedPage = page - 1;
        Pageable pageable = PageRequest.of(correctedPage, size, Sort.by("id").ascending());
        Page<CardResponse> cardResponses = cardService.getAllCards(cardStatus, userId, pageable);
        return ResponseEntity.ok(cardResponses);
    }

    /**
     * Получить карты текущего пользователя.
     * Доступно только пользователю с ролью "USER".
     *
     * @param status статус карт (необязательный параметр)
     * @param page   номер страницы (по умолчанию 1)
     * @param size   размер страницы (по умолчанию 10)
     * @return список карт текущего пользователя
     */
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

    /**
     * Преобразование строки статуса в тип CardStatus.
     *
     * @param status статус карты как строка
     * @return тип CardStatus
     * @throws IllegalArgumentException если строка не может быть преобразована в допустимый статус
     */
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

    /**
     * Заблокировать карту текущего пользователя.
     * Доступно только пользователю с ролью "USER".
     *
     * @param cardId ID карты, которую нужно заблокировать
     * @return сообщение о блокировке карты
     */
    @PatchMapping("/block-my-card/{cardId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> blockMyCard(@PathVariable("cardId") @NotNull @Min(1) Long cardId) {
        cardService.blockMyCard(cardId);
        return ResponseEntity.ok("Карта c ID " + cardId + " заблокирована!");
    }

    /**
     * Установить лимиты для карты.
     * Доступно только администраторам.
     *
     * @param cardId  ID карты, для которой нужно установить лимиты
     * @param request объект с данными для установки лимитов
     * @return сообщение об успешной установке лимитов
     */
    @PatchMapping("/set-card-limits/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> setCardLimits(@PathVariable Long cardId,
                                                @Valid @RequestBody CardLimitRequest request) {
        cardService.setCardLimits(cardId, request);
        return ResponseEntity.ok("Лимиты успешно установлены!");
    }

    /**
     * Снять наличные с карты.
     * Доступно только пользователю с ролью "USER".
     *
     * @param cardId  ID карты, с которой нужно снять деньги
     * @param request объект с данными для снятия наличных
     * @return сообщение об успешном снятии наличных
     */
    @PostMapping("/cash-withdraw/{cardId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> cashWithdraw(@PathVariable Long cardId,
                                               @Valid @RequestBody WithdrawRequest request) {
        cardService.cashWithdraw(cardId, request);
        return ResponseEntity.ok("Успешное снятие наличных средств!");
    }

    /**
     * Перевести деньги между картами.
     * Доступно только пользователю с ролью "USER".
     *
     * @param request объект с данными для перевода средств между картами
     * @return сообщение об успешном переводе
     */
    @PostMapping("/transfer-between-cards")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> transferBetweenCards(@Valid @RequestBody TransferRequest request) {
        transferService.transferBetweenCards(request);
        return ResponseEntity.ok("Успешный перевод между картами!");
    }
}
