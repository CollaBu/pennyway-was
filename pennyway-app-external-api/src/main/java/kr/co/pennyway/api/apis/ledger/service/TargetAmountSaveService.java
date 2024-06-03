package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.domain.common.redisson.DistributedLock;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorCode;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorException;
import kr.co.pennyway.domain.domains.target.service.TargetAmountService;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TargetAmountSaveService {
    private final TargetAmountService targetAmountService;

    @DistributedLock(key = "TargetAmount:User:" + "#user.id")
    public TargetAmount createTargetAmount(User user, LocalDate date) {
        if (targetAmountService.isExistsTargetAmountThatMonth(user.getId(), date)) {
            throw new TargetAmountErrorException(TargetAmountErrorCode.ALREADY_EXIST_TARGET_AMOUNT);
        }

        return targetAmountService.createTargetAmount(TargetAmount.of(-1, user));
    }
}
