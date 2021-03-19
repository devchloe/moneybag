package com.kakaopay.moneydistribution.repository;

import com.kakaopay.moneydistribution.model.MoneyBagChange;
import com.kakaopay.moneydistribution.model.MoneyBagChangeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoneyBagChangeRepository extends JpaRepository<MoneyBagChange, MoneyBagChangeId> {
}
