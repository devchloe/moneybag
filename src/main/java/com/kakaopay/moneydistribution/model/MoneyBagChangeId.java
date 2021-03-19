package com.kakaopay.moneydistribution.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@Getter
@Embeddable
public class MoneyBagChangeId implements Serializable {
    private MoneyBagId moneyBagId;

    @Column
    private long receiverId;

    public MoneyBagChangeId(MoneyBagId moneyBagId, long receiverId) {
        this.moneyBagId = moneyBagId;
        this.receiverId = receiverId;
    }
}
