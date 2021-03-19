package com.kakaopay.moneydistribution.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class MoneyBagChange implements Serializable {
    @EmbeddedId
    private MoneyBagChangeId moneyBagChangeId;

    @MapsId("moneyBagId")
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "ROOM_ID"),
        @JoinColumn(name = "TOKEN")
    })
    private MoneyBag moneyBag;

    @Column(nullable = false)
    private long receivedMoney;

    @Column(nullable = false)
    private LocalDateTime createdDateTime;

    @PrePersist
    private void onCreate() {
        this.createdDateTime = LocalDateTime.now();
    }

    public MoneyBagChange(MoneyBagChangeId moneyBagChangeId, long receivedMoney) {
        this.moneyBagChangeId = moneyBagChangeId;
        this.receivedMoney = receivedMoney;
    }
}
