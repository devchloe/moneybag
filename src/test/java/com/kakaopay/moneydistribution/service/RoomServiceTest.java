package com.kakaopay.moneydistribution.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {
    @InjectMocks
    private RoomService subject;

    @Test
    void whenIsMember_thenShouldReturnAlwaysTrue() {
        assertTrue(subject.isMember("any-room-id", 12345L));
    }
}