package com.bank.cardmanagement.web.controller;

import com.bank.cardmanagement.domain.service.UserService;
import com.bank.cardmanagement.dto.request.UserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для управления пользователями. Доступен только администраторам.
 */
@RestController
@RequestMapping("/card-management/")
public class UserController {

    /**
     * Сервис для операций с пользователями.
     */
    private final UserService userService;

    /**
     * Конструктор UserController с внедрением зависимости UserService.
     *
     * @param userService сервис для работы с пользователями
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Создание нового пользователя (только для ADMIN).
     *
     * @param userRequest данные пользователя
     * @return сообщение об успешном создании
     */
    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<String> createUser(@Valid @RequestBody UserRequest userRequest) {
        userService.createUser(userRequest);
        return ResponseEntity.ok("Пользователь успешно создан!");
    }

    /**
     * Удаление пользователя по ID (только для ADMIN).
     *
     * @param userId ID пользователя
     * @return сообщение об успешном удалении
     */
    @DeleteMapping("/delete-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<String> deleteUser(@PathVariable @NotNull @Min(1) Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("Пользователь успешно удален!");
    }
}
