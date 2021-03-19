package com.kakaopay.moneydistribution.service;

import com.kakaopay.moneydistribution.exception.MoneyBagExpiredException;
import com.kakaopay.moneydistribution.exception.MoneyBagNotFoundException;
import com.kakaopay.moneydistribution.exception.NoTargetException;
import com.kakaopay.moneydistribution.exception.RunOutOfMoneyException;
import com.kakaopay.moneydistribution.model.MoneyBag;
import com.kakaopay.moneydistribution.model.MoneyBagChange;
import com.kakaopay.moneydistribution.model.MoneyBagChangeId;
import com.kakaopay.moneydistribution.model.MoneyBagId;
import com.kakaopay.moneydistribution.repository.MoneyBagChangeRepository;
import com.kakaopay.moneydistribution.repository.MoneyBagQueryDslRepository;
import com.kakaopay.moneydistribution.repository.MoneyBagRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MoneyDistributionServiceTest {
    @InjectMocks
    private MoneyDistributionService subject;

    private final String argRoomId = "any-room-id";
    private final String argToken = "qwe";
    private final long argDistributorId = 12345L;
    private final long argReceiverId = 54321L;

    @Mock
    private MoneyBagRepository mockMoneyBagRepository;

    @Mock
    private MoneyBagChangeRepository mockMoneyBagChangeRepository;

    @Mock
    private MoneyBagQueryDslRepository mockMoneyBagQueryDslRepository;

    @Mock
    private TokenGenerator mockMoneyBagTokenGenerator;

    @Mock
    private FairMoneySplitter mockFairMoneySplitter;

    @Mock
    private RoomService mockRoomService;

    @Nested
    class createMoneyBag {
        private final long argAmount = 1000L;
        private final int argLimitOfReceivers = 4;

        private final String defaultToken = "qwe";
        private final int defaultTokenDigit = 3;

        @Captor
        private ArgumentCaptor<MoneyBag> moneyBagArgumentCaptor;

        @Test
        void whenCreatingMoneyBagWithNotDuplicateToken_thenShouldSaveSuccessfully() {
            given(mockFairMoneySplitter.splitMoney(argAmount, 4)).willReturn(250L);
            given(mockMoneyBagTokenGenerator.createToken(defaultTokenDigit)).willReturn(defaultToken);
            given(mockMoneyBagRepository.findById(new MoneyBagId(argRoomId, defaultToken))).willReturn(Optional.empty());

            String actualToken = subject.createMoneyBag(argRoomId, argDistributorId, argAmount, argLimitOfReceivers);

            assertEquals("qwe", actualToken);

            then(mockMoneyBagRepository).should().save(moneyBagArgumentCaptor.capture());
            MoneyBag captorValue = moneyBagArgumentCaptor.getValue();
            assertEquals("any-room-id", captorValue.getMoneyBagId().getRoomId());
            assertEquals("qwe", captorValue.getMoneyBagId().getToken());
            assertEquals(12345L, captorValue.getDistributorId());
            assertEquals(1000L, captorValue.getAmount());
            assertEquals(4, captorValue.getLimitOfReceivers());
            assertEquals(250L, captorValue.getSplittingMoney());
        }

        @Test
        void whenCreatedTokenInRoomExists_thenShouldReturnNewToken() {
            given(mockMoneyBagTokenGenerator.createToken(defaultTokenDigit))
                .willReturn(defaultToken)
                .willReturn("1@3");
            given(mockMoneyBagRepository.findById(new MoneyBagId(argRoomId, defaultToken)))
                .willReturn(Optional.of(MoneyBag.builder().moneyBagId(new MoneyBagId(argRoomId, defaultToken)).build()));
            given(mockFairMoneySplitter.splitMoney(argAmount, 4)).willReturn(250L);

            String actualToken = subject.createMoneyBag(argRoomId, argDistributorId, argAmount, argLimitOfReceivers);

            assertEquals("1@3", actualToken);

            then(mockMoneyBagRepository).should(times(2)).findById(any(MoneyBagId.class));
        }
    }

    @Nested
    class receiveMoney {

        @Captor
        private ArgumentCaptor<MoneyBagChange> moneyBagChangeArgumentCaptor;

        @Test
        void whenCorrectMoneyBagAndNoEmptyMoneyBag_thenShouldReturnReceivedMoney() {
            List<MoneyBagChange> moneyBagChanges = Collections.singletonList(
                MoneyBagChange.builder()
                    .moneyBagChangeId(new MoneyBagChangeId(new MoneyBagId(argRoomId, argToken), 98765)).build());
            given(mockMoneyBagQueryDslRepository.findAndLockMoneyBag(argRoomId, argToken)).willReturn(getCorrectMoneyBag());
            given(mockRoomService.isMember(anyString(), anyLong())).willReturn(true);
            given(mockMoneyBagQueryDslRepository.findMoneyBagChanges(argRoomId, argToken)).willReturn(moneyBagChanges);
            given(mockMoneyBagChangeRepository.save(any())).willReturn(MoneyBagChange.builder().receivedMoney(250).build());

            long actualReceivedMoney = subject.receiveMoney(argRoomId, argToken, argReceiverId);

            assertEquals(250L, actualReceivedMoney);
            then(mockMoneyBagChangeRepository).should(times(1)).save(moneyBagChangeArgumentCaptor.capture());
            MoneyBagChange captorValue = moneyBagChangeArgumentCaptor.getValue();
            assertEquals("any-room-id", captorValue.getMoneyBagChangeId().getMoneyBagId().getRoomId());
            assertEquals("qwe", captorValue.getMoneyBagChangeId().getMoneyBagId().getToken());
            assertEquals(54321L, captorValue.getMoneyBagChangeId().getReceiverId());
            assertEquals(250L, captorValue.getReceivedMoney());
        }

        @Test
        void whenRunOutOfMoney_thenShouldSeeRunOutOfMessage() {
            List<MoneyBagChange> moneyBagChanges = Arrays.asList(
                MoneyBagChange.builder().moneyBagChangeId(new MoneyBagChangeId(new MoneyBagId(argRoomId, argToken),
                    11111L)).build()
                , MoneyBagChange.builder().moneyBagChangeId(new MoneyBagChangeId(new MoneyBagId(argRoomId, argToken),
                    22222L)).build()
                , MoneyBagChange.builder().moneyBagChangeId(new MoneyBagChangeId(new MoneyBagId(argRoomId, argToken),
                    33333L)).build()
                , MoneyBagChange.builder().moneyBagChangeId(new MoneyBagChangeId(new MoneyBagId(argRoomId, argToken),
                    44444L)).build());
            given(mockMoneyBagQueryDslRepository.findAndLockMoneyBag(argRoomId, argToken)).willReturn(getCorrectMoneyBag());
            given(mockRoomService.isMember(anyString(), anyLong())).willReturn(true);
            given(mockMoneyBagQueryDslRepository.findMoneyBagChanges(argRoomId, argToken)).willReturn(moneyBagChanges);

            RunOutOfMoneyException exception = assertThrows(RunOutOfMoneyException.class,
                () -> subject.receiveMoney(argRoomId, argToken, argReceiverId));

            assertEquals("돈이 모두 소진되었습니다.", exception.getMessage());
            assertEquals(409, exception.getStatus());
        }

        @Test
        void whenNoExistMoneyBag_thenShouldSeeNotFoundMessage() {
            given(mockMoneyBagQueryDslRepository.findAndLockMoneyBag(argRoomId, argToken)).willReturn(null);

            MoneyBagNotFoundException exception = assertThrows(MoneyBagNotFoundException.class,
                () -> subject.receiveMoney(argRoomId, argToken, argReceiverId));

            assertEquals("유효하지 않은 뿌리기 건 입니다.", exception.getMessage());
            assertEquals(404, exception.getStatus());
        }

        @Test
        void whenDistributorTryToReceiveMoney_thenShouldSeeNoTargetMessage() {
            given(mockMoneyBagQueryDslRepository.findAndLockMoneyBag(argRoomId, argToken)).willReturn(getMyOwnMoneyBag());

            NoTargetException exception = assertThrows(NoTargetException.class,
                () -> subject.receiveMoney(argRoomId, argToken, argReceiverId));

            assertEquals("자신이 뿌리기한 건은 자신이 받을 수 없습니다.", exception.getMessage());
            assertEquals(400, exception.getStatus());
        }

        @Test
        void whenReceiverIsNotMemberOfRoom_thenShouldSeeNoTargetMessage() {
            given(mockMoneyBagQueryDslRepository.findAndLockMoneyBag(argRoomId, argToken)).willReturn(getCorrectMoneyBag());
            given(mockRoomService.isMember(anyString(), anyLong())).willReturn(false);

            NoTargetException exception = assertThrows(NoTargetException.class,
                () -> subject.receiveMoney(argRoomId, argToken, argReceiverId));

            assertEquals("대화방에 속한 사용자만 받을 수 있습니다.", exception.getMessage());
            assertEquals(400, exception.getStatus());
        }

        @Test
        void when10MinutesPassedAfterCreatingMoneyBag_thenShouldSeeExpiredMessage() {
            given(mockMoneyBagQueryDslRepository.findAndLockMoneyBag(argRoomId, argToken)).willReturn(getExpiredMoneyBag());
            given(mockRoomService.isMember(anyString(), anyLong())).willReturn(true);

            MoneyBagExpiredException exception = assertThrows(MoneyBagExpiredException.class,
                () -> subject.receiveMoney(argRoomId, argToken, argReceiverId));

            assertEquals("뿌린지 10분이 지나 돈을 받을 수 없습니다.", exception.getMessage());
            assertEquals(409, exception.getStatus());
        }

        private MoneyBag getMyOwnMoneyBag() {
            return MoneyBag.builder().distributorId(argReceiverId).build();
        }

        private MoneyBag getExpiredMoneyBag() {
            return MoneyBag.builder()
                .moneyBagId(new MoneyBagId(argRoomId, argToken))
                .createdDateTime(LocalDateTime.now().minusMinutes(20))
                .build();
        }

    }
    @Nested
    class getMoneyBagChanges {

        @Test
        void whenGetMoneyBagChanges_thenShouldReturnCurrentMoneyBagState() {
            LocalDateTime now = LocalDateTime.now();
            MoneyBag moneyBag = getCorrectMoneyBag();
            MoneyBag foundMoneyBag = moneyBag.toBuilder()
                .createdDateTime(now)
                .moneyBagChanges(Arrays.asList(
                    MoneyBagChange.builder()
                        .moneyBagChangeId(new MoneyBagChangeId(new MoneyBagId(argRoomId, argToken), 98765L)).receivedMoney(250L).build(),
                    MoneyBagChange.builder()
                        .moneyBagChangeId(new MoneyBagChangeId(new MoneyBagId(argRoomId, argToken), 65432L)).receivedMoney(250L).build()
                ))
                .build();
            given(mockMoneyBagQueryDslRepository.findMoneyBagIncludingChangesWithDistributorId(argRoomId, argToken, argDistributorId)).willReturn(foundMoneyBag);

            MoneyBag actualMoneyBag = subject.getMoneyBagChanges(argRoomId, argToken, argDistributorId);

            assertEquals(now, actualMoneyBag.getCreatedDateTime());
            assertEquals(1000L, actualMoneyBag.getAmount());

            assertEquals(2, actualMoneyBag.getMoneyBagChanges().size());
            assertEquals(98765L, actualMoneyBag.getMoneyBagChanges().get(0).getMoneyBagChangeId().getReceiverId());
            assertEquals(65432L, actualMoneyBag.getMoneyBagChanges().get(1).getMoneyBagChangeId().getReceiverId());
            assertEquals(250L, actualMoneyBag.getMoneyBagChanges().get(0).getReceivedMoney());
            assertEquals(250L, actualMoneyBag.getMoneyBagChanges().get(1).getReceivedMoney());
        }

        @Test
        void whenNoFoundMoneyBagWithRoomAndTokenAndDistributor_thenShouldSeeFailureMessage() {
            given(mockMoneyBagQueryDslRepository.findMoneyBagIncludingChangesWithDistributorId(argRoomId, argToken, argDistributorId)).willReturn(null);

            MoneyBagNotFoundException exception = assertThrows(MoneyBagNotFoundException.class,
                () -> subject.getMoneyBagChanges(argRoomId, argToken, argDistributorId));

            assertEquals("유효하지 않은 뿌리기 건 입니다.", exception.getMessage());
        }

        @Test
        void when7daysPassedAfterCreatingMoneyBag_thenShouldSeeFailureMessage() {
            given(mockMoneyBagQueryDslRepository.findMoneyBagIncludingChangesWithDistributorId(argRoomId, argToken, argDistributorId))
                .willReturn(MoneyBag.builder().createdDateTime(LocalDateTime.now().minusDays(8)).build());

            MoneyBagExpiredException exception = assertThrows(MoneyBagExpiredException.class,
                () -> subject.getMoneyBagChanges(argRoomId, argToken, argDistributorId));

            assertEquals("7일이 지나 조회할 수 없습니다.", exception.getMessage());
        }
    }

    private MoneyBag getCorrectMoneyBag() {
        return MoneyBag.builder()
            .moneyBagId(new MoneyBagId(argRoomId, argToken))
            .distributorId(12345L)
            .createdDateTime(LocalDateTime.now().minusMinutes(1))
            .amount(1000L)
            .limitOfReceivers(4)
            .splittingMoney(250L)
            .build();
    }
}