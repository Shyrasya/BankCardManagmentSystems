package com.bank.cardmanagement.web.controller;

import com.bank.cardmanagement.domain.service.UserService;
import com.bank.cardmanagement.dto.request.JwtRequest;
import com.bank.cardmanagement.dto.response.JwtResponse;
import com.bank.cardmanagement.dto.request.TokenRequest;
import com.bank.cardmanagement.entity.TokenType;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/card-management/auth")
public class AuthorizationController {
    private final UserService userService;

    public AuthorizationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody JwtRequest request) {
        JwtResponse response = userService.authorization(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-access")
    public ResponseEntity<?> updateAccessToken(@Valid @RequestBody TokenRequest request) {
        JwtResponse response = userService.updateToken(request.getRefreshToken(), TokenType.ACCESS);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-refresh")
    public ResponseEntity<?> updateRefreshToken(@Valid @RequestBody TokenRequest request) {
        JwtResponse response = userService.updateToken(request.getRefreshToken(), TokenType.REFRESH);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete-refresh")
    public ResponseEntity<?> deleteRefreshToken() {
        try {
            userService.deleteRefreshToken();
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok("Пользователь успешно вышел!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при выходе: " + e.getMessage());
        }
    }
}
