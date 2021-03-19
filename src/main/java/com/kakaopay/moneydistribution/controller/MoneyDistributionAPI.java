package com.kakaopay.moneydistribution.controller;

import com.kakaopay.moneydistribution.dto.*;
import com.kakaopay.moneydistribution.model.MoneyBag;
import com.kakaopay.moneydistribution.service.MoneyDistributionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class MoneyDistributionAPI {

    private final MoneyDistributionService moneyDistributionService;
    private final ModelMapper modelMapper;

    @PostMapping(
        value = "/api/v1/moneybags",
        consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE,
        headers = {"X-USER-ID", "X-ROOM-ID"})
    public TokenResponse distributeMoney(
        @RequestHeader("X-USER-ID") long userId, @RequestHeader("X-ROOM-ID") String roomId,
        @RequestBody @Valid MoneyBagRequest request) {

        String token = moneyDistributionService.createMoneyBag(roomId, userId, request.getAmount(), request.getLimitOfReceivers());
        return new TokenResponse(token);
    }

    @GetMapping(value = "/api/v1/moneybags/{token}", produces = APPLICATION_JSON_VALUE)
    public MoneyBagResponse getMoneyBagChanges(
        @RequestHeader("X-USER-ID") long userId, @RequestHeader("X-ROOM-ID") String roomId,
        @PathVariable String token) {

        MoneyBag moneyBag = moneyDistributionService.getMoneyBagChanges(roomId, token, userId);
        return modelMapper.map(moneyBag, MoneyBagResponse.class);
    }

    @PostMapping(value = "/api/v1/moneybags/{token}/receivers", produces = APPLICATION_JSON_VALUE)
    public MoneyReceiverResponse receiveMoney(
        @RequestHeader("X-USER-ID") long userId, @RequestHeader("X-ROOM-ID") String roomId,
        @PathVariable String token) {

        long receivedAmount = moneyDistributionService.receiveMoney(roomId, token, userId);
        return new MoneyReceiverResponse(receivedAmount);
    }
}
