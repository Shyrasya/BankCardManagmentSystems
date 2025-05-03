package com.bank.cardmanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI (Swagger UI) для добавления авторизации через JWT.
 * <p>
 * Добавляет кнопку "Authorize" в Swagger и делает все эндпоинты защищёнными,
 * кроме тех, где явно не указано требование авторизации.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Card Management API", version = "1.0")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    /**
     * Пустой конструктор по умолчанию.
     * Используется Spring для создания бина конфигурации.
     */
    public OpenApiConfig() {
    }
}