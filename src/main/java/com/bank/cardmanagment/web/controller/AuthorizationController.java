package com.bank.cardmanagment.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/card-managment/auth")
public class AuthorizationController {
    private final UserService userService;

    public AuthorizationController(UserService userService){
        this.userService = userService;
    }

    public
}
