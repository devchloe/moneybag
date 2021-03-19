package com.kakaopay.moneydistribution.service;

import org.springframework.stereotype.Service;

@Service
public class FairMoneySplitter {
    public long splitMoney(long amount, long pieces) {
        if (pieces == 0) {
            throw new ArithmeticException();
        }
        return (long) Math.ceil((double) amount / pieces);
    }
}
