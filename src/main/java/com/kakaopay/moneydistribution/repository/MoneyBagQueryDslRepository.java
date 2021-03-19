package com.kakaopay.moneydistribution.repository;

import com.kakaopay.moneydistribution.config.JpaPersistenceProperties;
import com.kakaopay.moneydistribution.model.MoneyBag;
import com.kakaopay.moneydistribution.model.MoneyBagChange;
import com.kakaopay.moneydistribution.model.QMoneyBag;
import com.kakaopay.moneydistribution.model.QMoneyBagChange;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MoneyBagQueryDslRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final JpaPersistenceProperties jpaPersistenceProperties;

    public MoneyBag findAndLockMoneyBag(String roomId, String token) {
        QMoneyBag moneyBag = QMoneyBag.moneyBag;
        return jpaQueryFactory.selectFrom(moneyBag)
            .where(
                moneyBag.moneyBagId.roomId.eq(roomId)
                , moneyBag.moneyBagId.token.eq(token))
            .setLockMode(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
            .setHint("javax.persistence.lock.timeout", jpaPersistenceProperties.getLockTimeout())
            .fetchOne();
    }

    public List<MoneyBagChange> findMoneyBagChanges(String roomId, String token) {
        QMoneyBagChange moneyBagChange = QMoneyBagChange.moneyBagChange;
        return jpaQueryFactory.selectFrom(moneyBagChange)
            .where(
                moneyBagChange.moneyBagChangeId.moneyBagId.roomId.eq(roomId)
                , moneyBagChange.moneyBagChangeId.moneyBagId.token.eq(token))
            .fetchAll()
            .fetch();
    }

    public MoneyBag findMoneyBagIncludingChangesWithDistributorId(String roomId, String token, long userId) {
        QMoneyBag moneyBag = QMoneyBag.moneyBag;
        QMoneyBagChange moneyBagChange = QMoneyBagChange.moneyBagChange;
        return jpaQueryFactory.selectFrom(moneyBag)
            .leftJoin(moneyBag.moneyBagChanges, moneyBagChange).fetchJoin()
            .where(
                moneyBag.moneyBagId.roomId.eq(roomId)
                , moneyBag.moneyBagId.token.eq(token)
                , moneyBag.distributorId.eq(userId))
            .fetchOne();
    }
}
