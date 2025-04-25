package com.bank.cardmanagment.domain.service;

import com.bank.cardmanagment.datasource.repository.UserRepository;
import com.bank.cardmanagment.model.JwtRequest;
import com.bank.cardmanagment.model.JwtResponse;
import com.bank.cardmanagment.model.TokenType;
import com.bank.cardmanagment.model.User;
import com.bank.cardmanagment.security.JwtProvider;
import com.bank.cardmanagment.security.JwtUtil;
import com.bank.cardmanagment.security.model.JwtAuthentication;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final JwtProvider provider;

    private final JwtUtil util;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    public UserService(UserRepository userRepository, JwtProvider provider, JwtUtil util) {
        this.userRepository = userRepository;
        this.provider = provider;
        this.util = util;
    }

    @Transactional
    public JwtResponse authorization(JwtRequest request) {
        String inputEmail = request.getEmail();
        String inputPassword = request.getPassword();
        User user = userRepository.findByEmail(inputEmail).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден!"));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            throw new BadCredentialsException("Неверный пароль!");
        }
        String accessToken = provider.generateAccessToken(user);
        String refreshToken = provider.generateRefreshToken(user);
        userRepository.updateRefreshTokenByUuid(user.getId(), refreshToken);
        return new JwtResponse(accessToken, refreshToken, accessTokenExpiration / 1000);
    }

    @Transactional
    public JwtResponse updateToken(String refreshToken, TokenType tokenType) {
        try {
            Claims claims = provider.validateToken(refreshToken);
            Long id = extractUserId(claims);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> unauthorized("Такого пользователя не существует!"));

            if (!provider.validateRefreshToken(user, refreshToken)) {
                throw unauthorized("Токен не совпадает с БД!");
            }

            if (tokenType == TokenType.ACCESS && !provider.isFreshToken(claims)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Срок годности refresh-токена истек!");
            }
            if (tokenType == TokenType.REFRESH) {
                refreshToken = provider.generateAccessToken(user);
                userRepository.updateRefreshTokenByUuid(user.getId(), refreshToken);
            }

            String newAccessToken = provider.generateAccessToken(user);
            return new JwtResponse(newAccessToken, refreshToken, accessTokenExpiration / 1000);
        } catch (SecurityException e) {
            throw unauthorized("Поддельный токен!");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Серверная ошибка при обновлении токена", e);
        }
    }

    private Long extractUserId(Claims claims) {
        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            throw unauthorized("Некорректный ID пользователя в токене!");
        }
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }

    @Transactional
    public void deleteRefreshToken() {
        Long id = getUserId();
        userRepository.deleteRefreshTokenByUuid(id);
    }

    private Long getUserId() {
        Authentication jwtAuth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(jwtAuth.getName());
    }

    public Claims providerValidateAccessToken(String accessToken) {
        return provider.validateAccessToken(accessToken);
    }

    public JwtAuthentication getJwtAuthentication(Claims claims) {
        return util.createAuthentication(claims);
    }

}
