package com.bank.cardmanagement.integration.controller;

import com.bank.cardmanagement.datasource.repository.UserRepository;
import com.bank.cardmanagement.dto.response.JwtResponse;
import com.bank.cardmanagement.entity.Role;
import com.bank.cardmanagement.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthorizationControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setUp() throws Exception {
        if (!userRepository.existsByEmail("testuser@example.com")) {
            User user = new User();
            user.setEmail("testuser@example.com");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole(Role.USER);
            userRepository.save(user);
        }

        JwtResponse tokens = loginAndGetTokens("testuser@example.com", "123456");
        this.accessToken = tokens.getAccessToken();
        this.refreshToken = tokens.getRefreshToken();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteByEmail("testuser@example.com");
    }

    @Test
    void updateAccessToken_shouldReturnJwtResponse_whenCredentialsAreValid() throws Exception {
        String jsonRequest = String.format("""
                    {
                      "refreshToken": "%s"
                    }
                """, refreshToken);

        mockMvc.perform(post("/card-management/auth/update-access")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void updateRefreshToken_shouldReturnJwtResponse_whenCredentialsAreValid() throws Exception {
        String jsonRequest = String.format("""
                    {
                        "refreshToken": "%s"
                    }
                """, refreshToken);

        mockMvc.perform(post("/card-management/auth/update-refresh")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void deleteRefreshToken_shouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(post("/card-management/auth/delete-refresh")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Пользователь успешно вышел!"));
    }
}
