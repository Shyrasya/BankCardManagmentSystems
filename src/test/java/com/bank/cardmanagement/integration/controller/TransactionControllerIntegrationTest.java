package com.bank.cardmanagement.integration.controller;

import com.bank.cardmanagement.datasource.repository.CardRepository;
import com.bank.cardmanagement.datasource.repository.TransactionRepository;
import com.bank.cardmanagement.datasource.repository.UserRepository;
import com.bank.cardmanagement.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessAdminToken;
    private String accessUserToken;
    private User user;
    private User admin;
    private Card card;

    @BeforeEach
    void setUp() throws Exception {
        if (!userRepository.existsByEmail("testuser@example.com")) {
            user = new User("testuser@example.com", passwordEncoder.encode("123456"), Role.USER);
            user = userRepository.save(user);
        } else {
            user = userRepository.findByEmail("testuser@example.com").orElseThrow();
        }

        if (!userRepository.existsByEmail("testadmin@example.com")) {
            admin = new User("testadmin@example.com", passwordEncoder.encode("123456"), Role.ADMIN);
            admin = userRepository.save(admin);
        } else {
            admin = userRepository.findByEmail("testadmin@example.com").orElseThrow();
        }

        card = new Card(user, "1111222233334444", LocalDate.now().plusYears(2), CardStatus.ACTIVE, BigDecimal.valueOf(1000), null, null);
        cardRepository.save(card);

        Transaction transaction = new Transaction(TransactionType.WITHDRAWAL, BigDecimal.valueOf(100), "ATM 156", LocalDateTime.now(), card);
        transactionRepository.save(transaction);

        accessUserToken = loginAndGetTokens(user.getEmail(), "123456").getAccessToken();
        accessAdminToken = loginAndGetTokens(admin.getEmail(), "123456").getAccessToken();
    }

    @Transactional
    @AfterEach
    void tearDown() {
        if (card != null) {
            transactionRepository.deleteByCard(card);
            cardRepository.delete(card);
        }
        userRepository.deleteByEmail("testadmin@example.com");
        userRepository.deleteByEmail("testuser@example.com");
    }

    @Test
    void getAllTransactions_shouldReturnTransactionsForAdmin() throws Exception {
        mockMvc.perform(get("/card-management/get-transactions")
                        .header("Authorization", "Bearer " + accessAdminToken)
                        .param("type", "WITHDRAWAL")
                        .param("cardId", card.getId().toString())
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getAllMyTransactions_shouldReturnTransactionsForUser() throws Exception {
        mockMvc.perform(get("/card-management/get-my-transactions")
                        .header("Authorization", "Bearer " + accessUserToken)
                        .param("cardId", card.getId().toString())
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getAllMyTransactions_shouldThrowExceptionForWrongTransactionType() throws Exception {
        mockMvc.perform(get("/card-management/get-my-transactions")
                        .header("Authorization", "Bearer " + accessUserToken)
                        .param("type", "WRONG")
                        .param("cardId", card.getId().toString()))
                .andExpect(status().isBadRequest());
    }
}
