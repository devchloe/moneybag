package com.kakaopay.moneydistribution.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class MoneyBagTokenGeneratorTest {
    @InjectMocks
    private MoneyBagTokenGenerator subject;

    @Test
    void whenCreateToken_thenReturnNewTokenWithGivenDigit() {
        String actual = subject.createToken(3);
        assertEquals(3, actual.length());

        actual = subject.createToken(5);
        assertEquals(5, actual.length());
    }
}