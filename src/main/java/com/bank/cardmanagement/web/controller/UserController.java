package com.bank.cardmanagement.web.controller;

import com.bank.cardmanagement.domain.service.UserService;
import com.bank.cardmanagement.dto.request.UserRequest;
import com.bank.cardmanagement.entity.CardStatus;
import com.bank.cardmanagement.entity.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/card-management/")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createUser(@Valid @RequestBody UserRequest userRequest){
        userService.createUser(userRequest);
        return ResponseEntity.ok("Пользователь успешно создан!");
    }

    @DeleteMapping("/delete-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable @NotNull @Min(1) Long userId){
        userService.deleteUser(userId);
        return ResponseEntity.ok("Пользователь успешно удален!");
    }
}
