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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AuthorizationControllerIntegrationTest {
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

        JwtResponse tokens = loginAndGetTokens();
        this.accessToken = tokens.getAccessToken();
        this.refreshToken = tokens.getRefreshToken();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteByEmail("testuser@example.com");
    }

    private JwtResponse loginAndGetTokens() throws Exception {
        String jsonRequest = """
                    {
                      "email": "testuser@example.com",
                      "password": "123456"
                    }
                """;

        String jsonResponse = mockMvc.perform(post("/card-management/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(jsonResponse, JwtResponse.class);
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
