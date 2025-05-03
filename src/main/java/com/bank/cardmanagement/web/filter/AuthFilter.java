package com.bank.cardmanagement.web.filter;

import com.bank.cardmanagement.domain.service.UserService;
import com.bank.cardmanagement.security.model.JwtAuthentication;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

/**
 * Фильтр для обработки JWT-аутентификации.
 * Пропускает только запросы с валидным access-токеном,
 * кроме эндпоинта /card-management/auth/login.
 */
public class AuthFilter extends GenericFilterBean {

    /**
     * Cервис для работы с пользователями и токенами
     */
    private final UserService userService;

    /**
     * Конструктор фильтра с внедрением UserService.
     *
     * @param userService сервис для работы с пользователями и токенами
     */
    public AuthFilter(UserService userService) {
        this.userService = userService;
    }

    /**
     * Проверяет наличие и валидность JWT access-токена в заголовке запроса.
     * Устанавливает аутентификацию в контекст безопасности.
     *
     * @param request  HTTP-запрос
     * @param response HTTP-ответ
     * @param chain    цепочка фильтров
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        if (path.equals("/card-management/auth/login") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/swagger-ui.html")) {
            chain.doFilter(request, response);
            return;
        }
        String header = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Необходима аутентификация!");
            return;
        }
        String accessToken = header.substring(7);
        Claims claims = userService.providerValidateAccessToken(accessToken);
        if (claims == null) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Невалидный access-токен!");
            return;
        }
        JwtAuthentication authentication = userService.getJwtAuthentication(claims);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }
}
