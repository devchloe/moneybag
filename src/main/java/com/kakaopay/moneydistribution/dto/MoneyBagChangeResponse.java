package com.kakaopay.moneydistribution.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MoneyBagChangeResponse {
    private long receiverId;
    private long receivedMoney;
}
