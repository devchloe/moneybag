package com.kakaopay.moneydistribution.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@Getter
public class MoneyBagId implements Serializable {
    @Column
    private String roomId;

    @Column
    private String token;

    public MoneyBagId(String roomId, String token) {
        this.roomId = roomId;
        this.token = token;
    }
}
