package com.kakaopay.moneydistribution.repository;

import com.kakaopay.moneydistribution.model.MoneyBag;
import com.kakaopay.moneydistribution.model.MoneyBagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoneyBagRepository extends JpaRepository<MoneyBag, MoneyBagId> {
}
