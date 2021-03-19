package com.kakaopay.moneydistribution.controller;

import com.kakaopay.moneydistribution.config.TestConfig;
import com.kakaopay.moneydistribution.dto.MoneyBagRequest;
import com.kakaopay.moneydistribution.model.MoneyBag;
import com.kakaopay.moneydistribution.model.MoneyBagChange;
import com.kakaopay.moneydistribution.model.MoneyBagChangeId;
import com.kakaopay.moneydistribution.model.MoneyBagId;
import com.kakaopay.moneydistribution.service.MoneyDistributionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MoneyDistributionAPI.class)
@Import(TestConfig.class)
class MoneyDistributionAPITest extends BaseAPITest {
    @MockBean
    private MoneyDistributionService mockMoneyDistributionService;

    private final String baseUri = "/api/v1/moneybags";
    private final long argUserId = 12345L;
    private final String argRoomId = "any-room-id";
    private final String defaultToken = "qwe";

    @Test
    void givenMoneyDistributorUriWithPost_whenMockMvc_thenShouldReturnToken() throws Exception {
        MoneyBagRequest request = new MoneyBagRequest(1000L, 4);
        String requestBodyString = mapper.writeValueAsString(request);
        given(mockMoneyDistributionService.createMoneyBag(argRoomId, argUserId, request.getAmount(),
            request.getLimitOfReceivers())).willReturn("qwe");

        mockMvc.perform(
            post(baseUri)
                .header("X-USER-ID", argUserId)
                .header("X-ROOM-ID", argRoomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyString)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("qwe"));
    }

    @Test
    void givenMoneyDistributionUriWithGet_whenMockMvc_thenShouldReturnMoneyBagState() throws Exception {
        LocalDateTime creationDateTime = LocalDateTime.now().minusMinutes(60);
        given(mockMoneyDistributionService.getMoneyBagChanges(argRoomId, defaultToken, argUserId))
            .willReturn(MoneyBag.builder()
                .moneyBagId(new MoneyBagId(argRoomId, defaultToken))
                .distributorId(argUserId)
                .amount(1000L)
                .limitOfReceivers(4)
                .splittingMoney(250L)
                .createdDateTime(creationDateTime)
                .moneyBagChanges(Arrays.asList(
                    MoneyBagChange.builder()
                        .moneyBagChangeId(new MoneyBagChangeId(new MoneyBagId(argRoomId, defaultToken), 33333L))
                        .receivedMoney(250L).build(),
                    MoneyBagChange.builder()
                        .moneyBagChangeId(new MoneyBagChangeId(new MoneyBagId(argRoomId, defaultToken), 44444L))
                        .receivedMoney(250L).build()
                ))
                .build()
            );

        mockMvc.perform(
            get(baseUri + "/{token}", defaultToken)
                .header("X-USER-ID", argUserId)
                .header("X-ROOM-ID", argRoomId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.createdDateTime").value(creationDateTime.toString()))
            .andExpect(jsonPath("$.amount").value("1000"))
            .andExpect(jsonPath("$.totalAmountOfReceivedMoney").value("500"))
            .andExpect(jsonPath("$.receivers.[0].receiverId").value("33333"))
            .andExpect(jsonPath("$.receivers.[0].receivedMoney").value("250"))
            .andExpect(jsonPath("$.receivers.[1].receiverId").value("44444"))
            .andExpect(jsonPath("$.receivers.[1].receivedMoney").value("250"));
    }

    @Test
    void givenMoneyReceiverUriWithPost_whenMockMvc_thenShouldReturnToken() throws Exception {
        given(mockMoneyDistributionService.receiveMoney(argRoomId, defaultToken, argUserId)).willReturn(250L);

        mockMvc.perform(
            post(baseUri + "/{token}/receivers", defaultToken)
                .header("X-USER-ID", argUserId)
                .header("X-ROOM-ID", argRoomId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.receivedAmount").value("250"));
    }
}