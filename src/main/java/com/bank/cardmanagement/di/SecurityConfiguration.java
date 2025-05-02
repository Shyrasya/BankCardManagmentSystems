package com.bank.cardmanagement.di;

import com.bank.cardmanagement.domain.service.UserService;
import com.bank.cardmanagement.security.JwtProvider;
import com.bank.cardmanagement.security.JwtUtil;
import com.bank.cardmanagement.web.filter.AuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Конфигурация безопасности для приложения.
 * Включает настройку фильтров безопасности, управление сессиями, а также работу с JWT.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    /**
     * Бин для провайдера JWT, отвечающий за создание и валидацию токенов.
     *
     * @return экземпляр {@link JwtProvider}, который используется для работы с JWT.
     */
    @Bean
    public JwtProvider provider() {
        return new JwtProvider();
    }

    /**
     * Бин для утилитного класса, работающего с JWT.
     *
     * @return экземпляр {@link JwtUtil}, который предоставляет вспомогательные методы для работы с JWT.
     */
    @Bean
    public JwtUtil util() {
        return new JwtUtil();
    }

    /**
     * Бин для фильтра авторизации, который перехватывает все входящие запросы
     * и проверяет наличие валидного JWT в заголовке.
     *
     * @param userService сервис для работы с пользователями.
     * @return экземпляр {@link AuthFilter}, выполняющий проверку JWT.
     */
    @Bean
    public AuthFilter authFilter(UserService userService) {
        return new AuthFilter(userService);
    }

    /**
     * Бин для настройки цепочки фильтров безопасности.
     * Включает настройку для пропуска запросов на авторизацию и работу с сессиями в состоянии Stateless.
     *
     * @param http       объект {@link HttpSecurity}, предоставляющий возможность настройки HTTP безопасности.
     * @param authFilter фильтр для авторизации.
     * @return настроенная цепочка фильтров безопасности.
     * @throws Exception если произошла ошибка при настройке безопасности.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthFilter authFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/card-management/auth/login")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Бин для кодировщика паролей, использующего алгоритм BCrypt.
     *
     * @return экземпляр {@link PasswordEncoder}, который используется для кодирования паролей.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
