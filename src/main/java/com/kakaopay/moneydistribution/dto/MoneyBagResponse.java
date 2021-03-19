package com.kakaopay.moneydistribution.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kakaopay.moneydistribution.model.MoneyBagChange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoneyBagResponse {
    private String createdDateTime;
    private long amount;
    private long totalAmountOfReceivedMoney;
    @JsonIgnore
    private List<MoneyBagChange> moneyBagChanges;
    private List<MoneyBagChangeResponse> receivers = new ArrayList<>();

    public void setMoneyBagChanges(List<MoneyBagChange> argMoneyBagChanges) {
        moneyBagChanges = argMoneyBagChanges;
        for (MoneyBagChange moneyBagChange : argMoneyBagChanges) {
            totalAmountOfReceivedMoney += moneyBagChange.getReceivedMoney();
            receivers.add(
                new MoneyBagChangeResponse(moneyBagChange.getMoneyBagChangeId().getReceiverId(), moneyBagChange.getReceivedMoney())
            );
        }
    }

}
