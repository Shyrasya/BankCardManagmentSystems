package com.bank.cardmanagement.domain.service;

import com.bank.cardmanagement.datasource.repository.UserRepository;
import com.bank.cardmanagement.dto.request.JwtRequest;
import com.bank.cardmanagement.dto.request.UserRequest;
import com.bank.cardmanagement.dto.response.JwtResponse;
import com.bank.cardmanagement.entity.Role;
import com.bank.cardmanagement.entity.TokenType;
import com.bank.cardmanagement.exception.EmailAlreadyExistsException;
import com.bank.cardmanagement.security.JwtProvider;
import com.bank.cardmanagement.security.JwtUtil;
import com.bank.cardmanagement.security.model.JwtAuthentication;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.bank.cardmanagement.entity.User;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider provider;

    @Mock
    private JwtUtil util;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final String email = "test@example.com";
    private final String password = "password";
    private final String encodedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMy.MrqK0X3YlE3eqXjMqVr7yPjQYv7JQnW";

    @Test
    void authorization_shouldReturnResponse(){
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setId(1L);
        JwtRequest request = new JwtRequest();
        request.setEmail(email);
        request.setPassword(password);
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        Mockito.when(provider.generateAccessToken(user)).thenReturn("access-token");
        Mockito.when(provider.generateRefreshToken(user)).thenReturn("refresh-token");
        Mockito.doNothing().when(userRepository).updateRefreshTokenByUuid(Mockito.anyLong(), Mockito.anyString());

        JwtResponse response = userService.authorization(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("access-token", response.getAccessToken());
        Assertions.assertEquals("refresh-token", response.getRefreshToken());
        Mockito.verify(userRepository).updateRefreshTokenByUuid(user.getId(), "refresh-token");
    }

    @Test
    void authorization_shouldThrowExceptionIfUserNotFound(){
        String wrongEmail = "wrong@test.com";
        JwtRequest request = new JwtRequest();
        request.setEmail(wrongEmail);
        request.setPassword(password);

        UsernameNotFoundException exception = Assertions.assertThrows(UsernameNotFoundException.class, () ->
                userService.authorization(request));

        Assertions.assertEquals("Пользователь не найден!", exception.getMessage());
    }

    @Test
    void authorization_shouldThrowExceptionIfWrongPassword(){
        String wrongPassword = "wrong";
        JwtRequest request = new JwtRequest();
        request.setEmail(email);
        request.setPassword(wrongPassword);
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setId(1L);
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        BadCredentialsException exception = Assertions.assertThrows(BadCredentialsException.class, () ->
                userService.authorization(request));

        Assertions.assertEquals("Неверный пароль!", exception.getMessage());
    }

    @Test
    void updateToken_shouldReturnResponseNewAccessToken(){
        Long userId = 1L;
        String refreshToken = "valid-refresh-token";
        Claims claims = Mockito.mock(Claims.class);
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        Mockito.when(provider.validateToken(refreshToken)).thenReturn(claims);
        Mockito.when(claims.getSubject()).thenReturn(userId.toString());
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(provider.validateRefreshToken(user, refreshToken)).thenReturn(true);
        Mockito.when(provider.isFreshToken(claims)).thenReturn(true);
        Mockito.when(provider.generateAccessToken(user)).thenReturn("new-access-token");

        JwtResponse response = userService.updateToken(refreshToken, TokenType.ACCESS);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("new-access-token", response.getAccessToken());
        Assertions.assertEquals("valid-refresh-token", response.getRefreshToken());
    }

    @Test
    void updateToken_shouldThrowExceptionIfRefreshTokenNotEqualDatabase(){
        Long userId = 1L;
        String refreshToken = "invalid-refresh-token";
        Claims claims = Mockito.mock(Claims.class);
        User user = new User();
        user.setId(userId);
        Mockito.when(provider.validateToken(refreshToken)).thenReturn(claims);
        Mockito.when(claims.getSubject()).thenReturn(userId.toString());
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(provider.validateRefreshToken(user, refreshToken)).thenReturn(false);

        ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class,
                () -> userService.updateToken(refreshToken, TokenType.ACCESS));

        Assertions.assertEquals("Токен не совпадает с БД!", exception.getReason());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void updateToken_shouldThrowExceptionIfTokenNotFresh(){
        Long userId = 1L;
        String refreshToken = "expired-refresh-token";
        Claims claims = Mockito.mock(Claims.class);
        User user = new User();
        user.setId(userId);
        Mockito.when(provider.validateToken(refreshToken)).thenReturn(claims);
        Mockito.when(claims.getSubject()).thenReturn(userId.toString());
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(provider.validateRefreshToken(user, refreshToken)).thenReturn(true);
        Mockito.when(provider.isFreshToken(claims)).thenReturn(false);

        ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class,
                () -> userService.updateToken(refreshToken, TokenType.ACCESS));

        Assertions.assertEquals("Срок годности refresh-токена истек!", exception.getReason());
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void updateToken_shouldThrowExceptionIfTokenIsFake(){
        String fakeToken = "fake-token";
        Mockito.when(provider.validateToken(fakeToken)).thenThrow(new SecurityException("Invalid signature"));

        ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class,
                () -> userService.updateToken(fakeToken, TokenType.ACCESS));

        Assertions.assertEquals("Поддельный токен!", exception.getReason());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void updateToken_shouldReturnResponseNewRefreshToken() {
        Long userId = 1L;
        String oldRefreshToken = "old-refresh-token";
        String newRefreshToken = "new-refresh-token";
        String newAccessToken = "new-access-token";
        Claims claims = Mockito.mock(Claims.class);
        User user = new User();
        user.setId(userId);
        Mockito.when(provider.validateToken(oldRefreshToken)).thenReturn(claims);
        Mockito.when(claims.getSubject()).thenReturn(userId.toString());
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(provider.validateRefreshToken(user, oldRefreshToken)).thenReturn(true);
        Mockito.when(provider.generateRefreshToken(user)).thenReturn(newRefreshToken);
        Mockito.doNothing().when(userRepository).updateRefreshTokenByUuid(Mockito.anyLong(), Mockito.anyString());
        Mockito.when(provider.generateAccessToken(user)).thenReturn(newAccessToken);

        JwtResponse response = userService.updateToken(oldRefreshToken, TokenType.REFRESH);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(newAccessToken, response.getAccessToken());
        Assertions.assertEquals(newRefreshToken, response.getRefreshToken());
    }

    @Test
    void updateToken_shouldThrowInternalServerErrorIfUpdateFails() {
        Long userId = 1L;
        String oldRefreshToken = "old-refresh-token";
        String newRefreshToken = "new-refresh-token";
        Claims claims = Mockito.mock(Claims.class);
        User user = new User();
        user.setId(userId);
        Mockito.when(provider.validateToken(oldRefreshToken)).thenReturn(claims);
        Mockito.when(claims.getSubject()).thenReturn(userId.toString());
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(provider.validateRefreshToken(user, oldRefreshToken)).thenReturn(true);
        Mockito.when(provider.generateRefreshToken(user)).thenReturn(newRefreshToken);
        Mockito.doThrow(new RuntimeException("DB error!"))
                .when(userRepository).updateRefreshTokenByUuid(userId, newRefreshToken);

        ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class,
                () -> userService.updateToken(oldRefreshToken, TokenType.REFRESH));

        Assertions.assertEquals("Серверная ошибка при обновлении токена!", exception.getReason());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    }

    @Test
    void updateToken_shouldThrowExceptionIfUserIdIncorrect(){
        String refreshToken = "some-refresh-token";
        Claims claims = Mockito.mock(Claims.class);
        Mockito.when(provider.validateToken(refreshToken)).thenReturn(claims);
        Mockito.when(claims.getSubject()).thenReturn("abc");

        ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class,
                () -> userService.updateToken(refreshToken, TokenType.ACCESS));

        Assertions.assertEquals("Некорректный ID пользователя в токене!", exception.getReason());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void deleteRefreshToken_shouldDeleteRefreshToken() {
        Long userId = 42L;
        SecurityContextHolder.clearContext();
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn(userId.toString());
        SecurityContext context = Mockito.mock(SecurityContext.class);
        Mockito.when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        userService.deleteRefreshToken();

        Mockito.verify(userRepository).deleteRefreshTokenByUuid(userId);
    }

    @Test
    void createUser_shouldCreate(){
        UserRequest request = new UserRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRole("USER");
        Mockito.when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("password123")).thenReturn("encodedPass");

        userService.createUser(request);

        Mockito.verify(userRepository).save(Mockito.argThat(user ->
                user.getEmail().equals("test@example.com") &&
                        user.getPassword().equals("encodedPass") &&
                        user.getRole() == Role.USER
        ));
    }

    @Test
    void createUser_shouldThrowExceptionIfEmailExists() {
        UserRequest request = new UserRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRole("USER");
        Mockito.when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        EmailAlreadyExistsException exception = Assertions.assertThrows(EmailAlreadyExistsException.class, () ->
                userService.createUser(request));

        Assertions.assertEquals("Пользователь с таким email уже существует!", exception.getMessage());
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void deleteUser_shouldDeleteUserIfExists() {
        Long userId = 1L;
        Mockito.when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        Mockito.verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_shouldThrowExceptionIfUserNotFound() {
        Long userId = 99L;
        Mockito.when(userRepository.existsById(userId)).thenReturn(false);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(userId));

        Assertions.assertEquals("Пользователь с ID 99 не найден!", exception.getMessage());
        Mockito.verify(userRepository, Mockito.never()).deleteById(Mockito.any());
    }

    @Test
    void providerValidateAccessToken_shouldCallProvider() {
        String token = "access.token";
        Claims claims = Mockito.mock(Claims.class);
        Mockito.when(provider.validateAccessToken(token)).thenReturn(claims);

        Claims result = userService.providerValidateAccessToken(token);

        Assertions.assertEquals(claims, result);
        Mockito.verify(provider).validateAccessToken(token);
    }

    @Test
    void getJwtAuthentication_shouldReturnAuthenticationFromUtil() {
        Claims claims = Mockito.mock(Claims.class);
        Long userId = 3L;
        GrantedAuthority role = new SimpleGrantedAuthority("ROLE_USER");
        JwtAuthentication authentication = new JwtAuthentication(userId, role);
        Mockito.when(util.createAuthentication(claims)).thenReturn(authentication);

        JwtAuthentication result = userService.getJwtAuthentication(claims);

        Assertions.assertEquals(authentication, result);
        Mockito.verify(util).createAuthentication(claims);
    }


}
