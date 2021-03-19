package com.kakaopay.moneydistribution.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MoneyBagRequest {
    @Min(1)
    private long amount;
    @Min(1)
    private int limitOfReceivers;
}
