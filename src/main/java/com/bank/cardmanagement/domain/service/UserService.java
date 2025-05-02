package com.bank.cardmanagement.domain.service;

import com.bank.cardmanagement.datasource.repository.UserRepository;
import com.bank.cardmanagement.dto.request.JwtRequest;
import com.bank.cardmanagement.dto.request.UserRequest;
import com.bank.cardmanagement.dto.response.JwtResponse;
import com.bank.cardmanagement.entity.Role;
import com.bank.cardmanagement.entity.TokenType;
import com.bank.cardmanagement.entity.User;
import com.bank.cardmanagement.exception.EmailAlreadyExistsException;
import com.bank.cardmanagement.security.JwtProvider;
import com.bank.cardmanagement.security.JwtUtil;
import com.bank.cardmanagement.security.model.JwtAuthentication;
import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

/**
 * Сервисный класс для работы с пользователями.
 * Отвечает за авторизацию, генерацию и обновление JWT токенов, а также за операции с refresh-токеном.
 */
@Service
public class UserService {

    /**
     * Репозиторий пользователей
     */
    private final UserRepository userRepository;

    /**
     * Компонент для шифрования и проверки паролей
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Провайдер для генерации и валидации JWT токенов
     */
    private final JwtProvider provider;

    /**
     * Утильный класс для создания объекта аутентификации из JWT
     */
    private final JwtUtil util;

    /**
     * Менеджер сущностей для управления кэшом JPA
     */
    private final EntityManager entityManager;

    /**
     * Время жизни access-токена в миллисекундах.
     * Значение считывается из файла конфигурации application.yml
     */
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * Конструктор сервиса пользователей.
     *
     * @param userRepository  репозиторий пользователей
     * @param passwordEncoder компонент для шифрования и проверки паролей
     * @param provider        провайдер для генерации и валидации JWT токенов
     * @param util            утильный класс для создания объекта аутентификации из JWT
     * @param entityManager   менеджер сущностей для управления кэшом JPA
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider provider, JwtUtil util, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.provider = provider;
        this.util = util;
        this.entityManager = entityManager;
    }

    /**
     * Авторизует пользователя по email и паролю.
     * Генерирует access и refresh токены.
     *
     * @param request объект с email и паролем
     * @return JwtResponse с access/refresh токенами и временем жизни
     */
    @Transactional
    public JwtResponse authorization(JwtRequest request) {
        String inputEmail = request.getEmail();
        String inputPassword = request.getPassword();
        User user = userRepository.findByEmail(inputEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден!"));
        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            throw new BadCredentialsException("Неверный пароль!");
        }
        String accessToken = provider.generateAccessToken(user);
        String refreshToken = provider.generateRefreshToken(user);
        userRepository.updateRefreshTokenByUuid(user.getId(), refreshToken);
        entityManager.clear();
        return new JwtResponse(accessToken, refreshToken, accessTokenExpiration / 1000);
    }

    /**
     * Обновляет токены по refresh-токену и типу запроса.
     * Генерирует новый access-токен, а при необходимости — новый refresh-токен.
     *
     * @param refreshToken refresh-токен
     * @param tokenType    тип запроса (ACCESS или REFRESH)
     * @return JwtResponse с новыми токенами
     */
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
                refreshToken = provider.generateRefreshToken(user);
                userRepository.updateRefreshTokenByUuid(user.getId(), refreshToken);
                entityManager.clear();
            }

            String newAccessToken = provider.generateAccessToken(user);
            return new JwtResponse(newAccessToken, refreshToken, accessTokenExpiration / 1000);
        } catch (SecurityException e) {
            throw unauthorized("Поддельный токен!");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Серверная ошибка при обновлении токена!", e);
        }
    }

    /**
     * Извлекает ID пользователя из claims.
     *
     * @param claims claims из токена
     * @return ID пользователя
     */
    private Long extractUserId(Claims claims) {
        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            throw unauthorized("Некорректный ID пользователя в токене!");
        }
    }

    /**
     * Генерирует исключение с HTTP-статусом 401 (UNAUTHORIZED).
     *
     * @param message сообщение ошибки
     * @return ResponseStatusException с кодом 401
     */
    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }

    /**
     * Удаляет refresh-токен текущего пользователя из базы.
     */
    @Transactional
    public void deleteRefreshToken() {
        Long id = getUserId();
        userRepository.deleteRefreshTokenByUuid(id);
    }

    /**
     * Получает ID текущего пользователя из контекста безопасности.
     *
     * @return ID пользователя
     */
    private Long getUserId() {
        Authentication jwtAuth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(jwtAuth.getName());
    }

    /**
     * Валидирует access-токен и возвращает Claims.
     *
     * @param accessToken access-токен
     * @return Claims из токена
     */
    public Claims providerValidateAccessToken(String accessToken) {
        return provider.validateAccessToken(accessToken);
    }

    /**
     * Создаёт объект аутентификации на основе токена.
     *
     * @param claims claims из токена
     * @return объект JwtAuthentication
     */
    public JwtAuthentication getJwtAuthentication(Claims claims) {
        return util.createAuthentication(claims);
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param userRequest объект с email, паролем и ролью
     */
    @Transactional
    public void createUser(UserRequest userRequest) {
        String email = userRequest.getEmail();
        boolean isUserExist = userRepository.existsByEmail(email);
        if (isUserExist) {
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует!");
        }
        parseRole(userRequest.getRole());
        User user = new User();
        user.setEmail(email);
        String rawPassword = userRequest.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        user.setRole(Role.valueOf(userRequest.getRole().toUpperCase()));
        userRepository.save(user);
    }

    /**
     * Проверяет корректность переданной роли пользователя.
     *
     * @param role роль в виде строки
     */
    private void parseRole(String role) {
        try {
            Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неверная роль пользователя! Доступные значения: " + Arrays.toString(Role.values()));
        }
    }

    /**
     * Удаляет пользователя по его ID.
     *
     * @param userId ID пользователя
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Пользователь с ID " + userId + " не найден!");
        }
        userRepository.deleteById(userId);
    }
}
