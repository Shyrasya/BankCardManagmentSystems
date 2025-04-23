package com.bank.cardmanagment.domain.service;

import com.bank.cardmanagment.datasource.repository.UserRepository;
import com.bank.cardmanagment.model.JwtRequest;
import com.bank.cardmanagment.model.JwtResponse;
import com.bank.cardmanagment.model.User;

import com.bank.cardmanagment.security.JwtProvider;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final JwtProvider provider;

    private final JwtUtil util;

    public enum TokenType {
        ACCESS, REFRESH
    }

    public UserService(UserRepository userRepository, JwtProvider provider, JwtUtil util){
        this.userRepository = userRepository;
        this.provider = provider;
        this.util = util;
    }

    @Transactional
    public JwtResponse authorization(JwtRequest request){
        String inputLogin = request.getLogin();
        String inputPassword = request.getPassword();
        User user = userRepository.findByLogin(inputLogin).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден!"));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(inputPassword, user.getPassword())){
            throw new BadCredentialsException("Неверный пароль!");
        }
        String accessToken = provider.generateAccessToken(user);
        String refreshToken = provider.generateRefreshToken(user);
        userRepository.updateRefreshTokenByUuid(user.getId(), refreshToken);
        return new JwtResponse(accessToken, refreshToken, getExpiresAccessToken(accessToken));
    }

    private long getExpiresAccessToken(String accessToken){
        Claims accessClaims = provider.getClaims(accessToken);
        return accessClaims.getExpiration().getTime() / 1000;
    }

//    public JwtResponse updateAccessToken(String refreshToken){
//
//    }

}
