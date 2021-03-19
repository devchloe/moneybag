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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MoneyDistributionService {
    private static final long MONEY_BAG_RECEIVE_EXPIRATION_MINUTES = 10;
    private static final long MONEY_BAG_READ_EXPIRATION_DAYS = 7;
    private static final int TOKEN_DIGIT = 3;
    private final MoneyBagRepository moneyBagRepository;
    private final MoneyBagChangeRepository moneyBagChangeRepository;
    private final TokenGenerator moneyBagTokenGenerator;
    private final FairMoneySplitter fairMoneySplitter;
    private final MoneyBagQueryDslRepository moneyBagQueryDslRepository;
    private final RoomService roomService;

    public String createMoneyBag(String roomId, long distributorId, long amount, int limitOfReceivers) {
        MoneyBag moneyBag = makeMoneyBag(roomId, distributorId, amount, limitOfReceivers);
        moneyBagRepository.save(moneyBag);
        return moneyBag.getMoneyBagId().getToken();
    }

    @Transactional
    public long receiveMoney(String roomId, String token, long receiverId) {
        MoneyBag foundMoneyBag = moneyBagQueryDslRepository.findAndLockMoneyBag(roomId, token);
        checkIfMoneyBagIsValidAndReceiverIsQualified(foundMoneyBag, receiverId);

        List<MoneyBagChange> moneyBagChanges = moneyBagQueryDslRepository.findMoneyBagChanges(roomId, token);

        MoneyBagChange foundOrCreatedMoneyBagChange =
            moneyBagChanges.stream()
                .filter(change -> change.getMoneyBagChangeId().getReceiverId() == receiverId)
                .findFirst()
                .orElseGet(() -> {
                    if (foundMoneyBag.getLimitOfReceivers() == moneyBagChanges.size()) {
                        throw new RunOutOfMoneyException();
                    }

                    return moneyBagChangeRepository.save(createMoneyBagChange(roomId, token, receiverId, foundMoneyBag));
                });

        return foundOrCreatedMoneyBagChange.getReceivedMoney();
    }

    @Transactional(readOnly = true)
    public MoneyBag getMoneyBagChanges(String roomId, String token, long userId) {
        MoneyBag foundMoneyBag = moneyBagQueryDslRepository.findMoneyBagIncludingChangesWithDistributorId(roomId, token, userId);

        if (foundMoneyBag == null) {
            throw new MoneyBagNotFoundException();
        }

        if (isExpiredReadingMoneyBag(foundMoneyBag.getCreatedDateTime())) {
            throw new MoneyBagExpiredException(String.format("%d일이 지나 조회할 수 없습니다.", MONEY_BAG_READ_EXPIRATION_DAYS));
        }

        return foundMoneyBag;
    }

    private MoneyBag makeMoneyBag(String roomId, long distributorId, long amount, int limitOfReceivers) {
        MoneyBagId moneyBagId = createMoneyBagId(roomId);
        long splittingMoney = fairMoneySplitter.splitMoney(amount, limitOfReceivers);

        return MoneyBag.builder()
            .moneyBagId(moneyBagId)
            .distributorId(distributorId)
            .amount(amount)
            .limitOfReceivers(limitOfReceivers)
            .splittingMoney(splittingMoney)
            .build();
    }

    private MoneyBagId createMoneyBagId(String roomId) {
        String token;
        MoneyBagId moneyBagId;

        do {
            token = moneyBagTokenGenerator.createToken(TOKEN_DIGIT);
            moneyBagId = createMoneyBagId(roomId, token);
        } while (moneyBagRepository.findById(moneyBagId).isPresent());

        return moneyBagId;
    }

    private MoneyBagId createMoneyBagId(String roomId, String token) {
        return new MoneyBagId(roomId, token);
    }

    private MoneyBagChange createMoneyBagChange(String roomId, String token, long receiverId, MoneyBag foundMoneyBag) {
        MoneyBagId moneyBagId = new MoneyBagId(roomId, token);
        MoneyBagChangeId moneyBagChangeId = new MoneyBagChangeId(moneyBagId, receiverId);
        MoneyBagChange moneyBagChange = new MoneyBagChange(moneyBagChangeId, foundMoneyBag.getSplittingMoney());
        moneyBagChange.setMoneyBag(foundMoneyBag);
        return moneyBagChange;
    }

    private void checkIfMoneyBagIsValidAndReceiverIsQualified(MoneyBag foundMoneyBag, long receiverId) {
        if (foundMoneyBag == null) {
            throw new MoneyBagNotFoundException();
        }

        if (foundMoneyBag.getDistributorId() == receiverId) {
            throw new NoTargetException("자신이 뿌리기한 건은 자신이 받을 수 없습니다.");
        }

        if (!roomService.isMember(foundMoneyBag.getMoneyBagId().getRoomId(), receiverId)) {
            throw new NoTargetException("대화방에 속한 사용자만 받을 수 있습니다.");
        }

        if (isExpiredReceivingMoney(foundMoneyBag.getCreatedDateTime())) {
            throw new MoneyBagExpiredException(String.format("뿌린지 %d분이 지나 돈을 받을 수 없습니다.",
                MONEY_BAG_RECEIVE_EXPIRATION_MINUTES));
        }
    }

    private boolean isExpiredReadingMoneyBag(LocalDateTime dateTime) {
        return LocalDateTime.now().isAfter(dateTime.plusDays(MONEY_BAG_READ_EXPIRATION_DAYS));
    }

    private boolean isExpiredReceivingMoney(LocalDateTime dateTime) {
        return LocalDateTime.now().isAfter(dateTime.plusMinutes(MONEY_BAG_RECEIVE_EXPIRATION_MINUTES));
    }
}
