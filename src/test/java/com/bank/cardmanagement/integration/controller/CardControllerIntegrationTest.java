package com.bank.cardmanagement.integration.controller;

import com.bank.cardmanagement.datasource.repository.CardRepository;
import com.bank.cardmanagement.datasource.repository.UserRepository;
import com.bank.cardmanagement.dto.request.CardLimitRequest;
import com.bank.cardmanagement.dto.request.CardRequest;
import com.bank.cardmanagement.dto.request.TransferRequest;
import com.bank.cardmanagement.dto.request.WithdrawRequest;
import com.bank.cardmanagement.entity.CardStatus;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CardControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessAdminToken;
    private String accessUserToken;
    private User admin;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        if (!userRepository.existsByEmail("testadmin@example.com")) {
            admin = new User("testadmin@example.com", passwordEncoder.encode("123456"), Role.ADMIN);
            admin = userRepository.save(admin);
        } else {
            admin = userRepository.findByEmail("testadmin@example.com").orElseThrow();
        }

        if (!userRepository.existsByEmail("testuser@example.com")) {
            user = new User("testuser@example.com", passwordEncoder.encode("123456"), Role.USER);
            user = userRepository.save(user);
        } else {
            user = userRepository.findByEmail("testuser@example.com").orElseThrow();
        }

        accessAdminToken = loginAndGetTokens(admin.getEmail(), "123456").getAccessToken();
        accessUserToken = loginAndGetTokens(user.getEmail(), "123456").getAccessToken();
    }

    @AfterEach
    void tearDown() {
        cardRepository.deleteAll();
        userRepository.deleteByEmail("testadmin@example.com");
        userRepository.deleteByEmail("testuser@example.com");
    }

    private Long createCard(Long userId, String accessToken) throws Exception {
        CardRequest request = new CardRequest();
        request.setUserId(userId);
        String jsonRequest = objectMapper.writeValueAsString(request);

        String responseJson = mockMvc.perform(post("/card-management/create-card")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseJson).get("id").asLong();
    }

    private void blockCard(Long cardId) throws Exception {
        mockMvc.perform(patch("/card-management/block-card/{cardId}", cardId)
                        .header("Authorization", "Bearer " + accessAdminToken))
                .andExpect(status().isOk());
    }


    @Test
    void createCard_shouldReturnResponseForAdmin() throws Exception {
        CardRequest request = new CardRequest();
        request.setUserId(admin.getId());

        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/card-management/create-card")
                        .header("Authorization", "Bearer " + accessAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.maskedCardNumber").exists())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void deleteCard_shouldReturnSuccessfulResponseForAdmin() throws Exception {
        Long cardId = createCard(admin.getId(), accessAdminToken);

        mockMvc.perform(delete("/card-management/delete-card/{cardId}", cardId)
                        .header("Authorization", "Bearer " + accessAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Карта с ID " + cardId + " была успешно удалена!"));
    }

    @Test
    void blockCard_shouldReturnSuccessMessage() throws Exception {
        Long cardId = createCard(admin.getId(), accessAdminToken);

        mockMvc.perform(patch("/card-management/block-card/{cardId}", cardId)
                        .header("Authorization", "Bearer " + accessAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Карта c ID " + cardId + " заблокирована!"));
    }

    @Test
    void activateCard_shouldReturnSuccessMessage() throws Exception {
        Long cardId = createCard(admin.getId(), accessAdminToken);

        blockCard(cardId);

        mockMvc.perform(patch("/card-management/activate-card/{cardId}", cardId)
                        .header("Authorization", "Bearer " + accessAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Карта с ID " + cardId + " активирована!"));
    }

    @Test
    void getAllCards_shouldReturnListWith2CardsForAdmin() throws Exception {
        createCard(admin.getId(), accessAdminToken);
        createCard(admin.getId(), accessAdminToken);

        mockMvc.perform(get("/card-management/get-cards")
                        .param("status", "ACTIVE")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + accessAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.content[1].status").value("ACTIVE"));
    }

    @Test
    void getAllCards_withEmptyStatus_shouldReturnAll() throws Exception {
        createCard(admin.getId(), accessAdminToken);
        createCard(admin.getId(), accessAdminToken);

        mockMvc.perform(get("/card-management/get-cards")
                        .param("status", "")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + accessAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getAllCards_withInvalidStatus_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/card-management/get-cards")
                        .param("status", "NOT_A_STATUS")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + accessAdminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllMyCards_withValidStatus_shouldReturnFiltered() throws Exception {
        Long cardId = createCard(user.getId(), accessAdminToken);

        blockCard(cardId);

        mockMvc.perform(get("/card-management/get-my-cards")
                        .param("status", "BLOCKED")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + accessUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("BLOCKED"));
    }


    @Test
    void getAllMyCards_withEmptyStatus_shouldReturnAll() throws Exception {
        createCard(user.getId(), accessAdminToken);
        createCard(user.getId(), accessAdminToken);

        mockMvc.perform(get("/card-management/get-my-cards")
                        .param("status", "")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + accessUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getAllMyCards_withInvalidStatus_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/card-management/get-my-cards")
                        .param("status", "NOT_A_STATUS")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + accessUserToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blockMyCard_shouldReturnSuccessMessage() throws Exception {
        Long cardId = createCard(user.getId(), accessAdminToken);

        mockMvc.perform(patch("/card-management/block-my-card/{cardId}", cardId)
                        .header("Authorization", "Bearer " + accessUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Карта c ID " + cardId + " заблокирована!"));
    }

    @Test
    void setCardLimits_shouldReturnSuccessMessage() throws Exception {
        Long cardId = createCard(user.getId(), accessAdminToken);

        CardLimitRequest request = new CardLimitRequest();
        request.setDailyLimit(BigDecimal.valueOf(10000));
        request.setMonthlyLimit(BigDecimal.valueOf(300000));
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/card-management/set-card-limits/{cardId}", cardId)
                        .header("Authorization", "Bearer " + accessAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Лимиты успешно установлены!"));
    }

    @Test
    void cashWithdraw_shouldReturnSuccessMessage() throws Exception {
        Long cardId = createCard(user.getId(), accessAdminToken);

        cardRepository.findById(cardId).ifPresent(card -> {
            card.setDailyLimit(BigDecimal.valueOf(1000));
            card.setMonthlyLimit(BigDecimal.valueOf(100000));
            card.setBalance(BigDecimal.valueOf(500));
            card.setStatus(CardStatus.ACTIVE);
            cardRepository.save(card);
        });

        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(BigDecimal.valueOf(300));
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/card-management/cash-withdraw/{cardId}", cardId)
                        .header("Authorization", "Bearer " + accessUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Успешное снятие наличных средств!"));
    }

    @Test
    void transferBetweenCards_shouldReturnSuccessMessage() throws Exception {
        Long sourceCardId = createCard(user.getId(), accessAdminToken);
        Long destinationCardId = createCard(user.getId(), accessAdminToken);

        cardRepository.findById(sourceCardId).ifPresent(card -> {
            card.setDailyLimit(BigDecimal.valueOf(1000));
            card.setMonthlyLimit(BigDecimal.valueOf(10000));
            card.setBalance(BigDecimal.valueOf(600));
            card.setStatus(CardStatus.ACTIVE);
            cardRepository.save(card);
        });

        cardRepository.findById(destinationCardId).ifPresent(card -> {
            card.setStatus(CardStatus.ACTIVE);
            cardRepository.save(card);
        });

        TransferRequest request = new TransferRequest();
        request.setSourceCardId(sourceCardId);
        request.setDestinationCardId(destinationCardId);
        request.setAmount(BigDecimal.valueOf(400));
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/card-management/transfer-between-cards")
                        .header("Authorization", "Bearer " + accessUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Успешный перевод между картами!"));
    }


}
