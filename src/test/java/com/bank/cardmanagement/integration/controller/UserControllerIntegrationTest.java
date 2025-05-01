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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class UserControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessAdminToken;

    @BeforeEach
    void setUp() throws Exception {
        if (!userRepository.existsByEmail("testadmin@example.com")) {
            User user = new User();
            user.setEmail("testadmin@example.com");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole(Role.ADMIN);
            userRepository.save(user);
        }

        JwtResponse tokens = loginAndGetTokens();
        this.accessAdminToken = tokens.getAccessToken();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteByEmail("testadmin@example.com");
        userRepository.deleteByEmail("newuser@example.com");
    }

    private JwtResponse loginAndGetTokens() throws Exception {
        String jsonRequest = """
                    {
                      "email": "testadmin@example.com",
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
    void createUser_shouldCreateSuccessfully() throws Exception {
        String jsonRequest = """
                    {
                        "email": "newuser@example.com",
                                "password": "userpass",
                                "role": "USER"
                    }
                """;

        mockMvc.perform(post("/card-management/create-user")
                        .header("Authorization", "Bearer " + accessAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Пользователь успешно создан!"));
    }

    @Test
    void deleteUser_shouldDeleteSuccessfully() throws Exception {
        User user = new User();
        user.setEmail("newuser@example.com");
        user.setPassword(passwordEncoder.encode("userpass"));
        user.setRole(Role.USER);
        userRepository.save(user);

        mockMvc.perform(delete("/card-management/delete-user/" + user.getId())
                        .header("Authorization", "Bearer " + accessAdminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Пользователь успешно удален!"));
    }
}