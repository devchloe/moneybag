package com.kakaopay.moneydistribution.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MoneyBag implements Serializable {

    @EmbeddedId
    private MoneyBagId moneyBagId;

    @Version
    private long version;

    @Column(nullable = false)
    private long distributorId;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private int limitOfReceivers;

    @Column(nullable = false)
    private long splittingMoney;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdDateTime;

    @OneToMany(mappedBy = "moneyBag", orphanRemoval = true)
    List<MoneyBagChange> moneyBagChanges;
}
