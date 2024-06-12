package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.domain.common.redisson.DistributedLock;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorCode;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorException;
import kr.co.pennyway.domain.domains.target.service.TargetAmountService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TargetAmountSaveService {
    private final UserService userService;
    private final TargetAmountService targetAmountService;

    @DistributedLock(key = "#key.concat(#userId).concat('_').concat(#date.getYear()).concat('-').concat(#date.getMonthValue())")
    public TargetAmount createTargetAmount(String key, Long userId, LocalDate date) {
        User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));

        if (targetAmountService.isExistsTargetAmountThatMonth(user.getId(), date)) {
            log.info("{}에 대한 날짜의 목표 금액이 이미 존재합니다.", date);
            throw new TargetAmountErrorException(TargetAmountErrorCode.ALREADY_EXIST_TARGET_AMOUNT);
        }

        return targetAmountService.createTargetAmount(TargetAmount.of(-1, user));
    }

    @Transactional
    public TargetAmount updateTargetAmount(Long targetAmountId, Integer amount) {
        TargetAmount targetAmount = targetAmountService.readTargetAmount(targetAmountId)
                .orElseThrow(() -> new TargetAmountErrorException(TargetAmountErrorCode.NOT_FOUND_TARGET_AMOUNT));

        if (!targetAmount.isThatMonth()) {
            throw new TargetAmountErrorException(TargetAmountErrorCode.INVALID_TARGET_AMOUNT_DATE);
        }

        targetAmount.updateAmount(amount);

        return targetAmount;
    }
}
