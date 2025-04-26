package com.bank.cardmanagment.di;

import com.bank.cardmanagment.datasource.repository.UserRepository;
import com.bank.cardmanagment.domain.service.UserService;
import com.bank.cardmanagment.security.JwtProvider;
import com.bank.cardmanagment.security.JwtUtil;
import com.bank.cardmanagment.web.filter.AuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public UserService userService(UserRepository userRepository, JwtProvider provider, JwtUtil util) {
        return new UserService(userRepository, provider, util);
    }

    @Bean
    public JwtProvider provider() {
        return new JwtProvider();
    }

    @Bean
    public JwtUtil util() {
        return new JwtUtil();
    }

    @Bean
    public AuthFilter authFilter(UserService userService) {
        return new AuthFilter(userService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthFilter authFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/card-managment/auth/login")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
