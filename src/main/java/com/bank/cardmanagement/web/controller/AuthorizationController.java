package com.bank.cardmanagement.web.controller;

import com.bank.cardmanagement.domain.service.UserService;
import com.bank.cardmanagement.dto.request.JwtRequest;
import com.bank.cardmanagement.dto.request.TokenRequest;
import com.bank.cardmanagement.dto.response.JwtResponse;
import com.bank.cardmanagement.entity.TokenType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для управления авторизацией и обновлением токенов.
 * Предоставляет эндпоинты для входа пользователя, обновления токенов и выхода.
 */
@RestController
@RequestMapping("/card-management/auth")
public class AuthorizationController {

    /**
     * Сервис для работы с пользователями.
     */
    private final UserService userService;

    /**
     * Конструктор контроллера.
     *
     * @param userService сервис для работы с пользователями
     */
    public AuthorizationController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Авторизация пользователя с получением JWT токена.
     *
     * @param request объект с данными для авторизации (например, логин и пароль)
     * @return JWT токен для доступа к системе
     */
    @PostMapping("/login")
    @Operation(security = @SecurityRequirement(name = ""))
    public ResponseEntity<?> login(@Valid @RequestBody JwtRequest request) {
        JwtResponse response = userService.authorization(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Обновление токена доступа на основе refresh токена.
     *
     * @param request объект с refresh токеном
     * @return новый access токен
     */
    @PostMapping("/update-access")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> updateAccessToken(@Valid @RequestBody TokenRequest request) {
        JwtResponse response = userService.updateToken(request.getRefreshToken(), TokenType.ACCESS);
        return ResponseEntity.ok(response);
    }

    /**
     * Обновление refresh токена.
     *
     * @param request объект с refresh токеном
     * @return новый refresh токен
     */
    @PostMapping("/update-refresh")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> updateRefreshToken(@Valid @RequestBody TokenRequest request) {
        JwtResponse response = userService.updateToken(request.getRefreshToken(), TokenType.REFRESH);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление refresh токена и выход пользователя из системы.
     * Очищает контекст безопасности.
     *
     * @return сообщение о выходе пользователя
     */
    @PostMapping("/delete-refresh")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> deleteRefreshToken() {
        userService.deleteRefreshToken();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Пользователь успешно вышел!");
    }
}
