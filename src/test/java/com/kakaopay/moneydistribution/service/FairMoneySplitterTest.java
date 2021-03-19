package com.kakaopay.moneydistribution.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FairMoneySplitterTest {
    @InjectMocks
    private FairMoneySplitter subject;

    @Test
    void givenZeroPieces_whenSplitMoney_thenShouldNotSplitMoney() {
        assertThrows(ArithmeticException.class, () -> subject.splitMoney(1000, 0));
    }

    @Test
    void givenCorrectInputs_whenSplitMoney_thenShouldReturnSplittingMoney() {
        long actual = subject.splitMoney(1000, 3);

        assertEquals(334, actual);
    }

}