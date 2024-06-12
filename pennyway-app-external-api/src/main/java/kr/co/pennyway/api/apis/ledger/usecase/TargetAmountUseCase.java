package kr.co.pennyway.api.apis.ledger.usecase;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.api.apis.ledger.mapper.TargetAmountMapper;
import kr.co.pennyway.api.apis.ledger.service.TargetAmountSaveService;
import kr.co.pennyway.api.apis.ledger.service.TargetAmountSearchService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.common.redisson.DistributedLockPrefix;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorCode;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorException;
import kr.co.pennyway.domain.domains.target.service.TargetAmountService;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class TargetAmountUseCase {
    private final UserService userService;
    private final TargetAmountService targetAmountService;
    private final SpendingService spendingService;

    private final TargetAmountSaveService targetAmountSaveService;
    private final TargetAmountSearchService targetAmountSearchService;

    @Transactional
    public TargetAmountDto.TargetAmountInfo createTargetAmount(Long userId, int year, int month) {
        TargetAmount targetAmount = targetAmountSaveService.createTargetAmount(DistributedLockPrefix.TARGET_AMOUNT_USER, userId, LocalDate.of(year, month, 1));

        return TargetAmountDto.TargetAmountInfo.from(targetAmount);
    }

    @Transactional(readOnly = true)
    public TargetAmountDto.WithTotalSpendingRes getTargetAmountAndTotalSpending(Long userId, LocalDate date) {
        return targetAmountSearchService.readTargetAmountAndTotalSpending(userId, date);
    }

    @Transactional(readOnly = true)
    public List<TargetAmountDto.WithTotalSpendingRes> getTargetAmountsAndTotalSpendings(Long userId, LocalDate date) {
        return targetAmountSearchService.readTargetAmountsAndTotalSpendings(userId, date);
    }

    @Transactional(readOnly = true)
    public TargetAmountDto.RecentTargetAmountRes getRecentTargetAmount(Long userId) {
        return TargetAmountMapper.toRecentTargetAmountResponse(targetAmountSearchService.readRecentTargetAmount(userId));
    }

    @Transactional
    public TargetAmountDto.TargetAmountInfo updateTargetAmount(Long targetAmountId, Integer amount) {
        TargetAmount targetAmount = targetAmountService.readTargetAmount(targetAmountId)
                .orElseThrow(() -> new TargetAmountErrorException(TargetAmountErrorCode.NOT_FOUND_TARGET_AMOUNT));

        if (!targetAmount.isThatMonth()) {
            throw new TargetAmountErrorException(TargetAmountErrorCode.INVALID_TARGET_AMOUNT_DATE);
        }

        targetAmount.updateAmount(amount);

        return TargetAmountDto.TargetAmountInfo.from(targetAmount);
    }

    @Transactional
    public void deleteTargetAmount(Long targetAmountId) {
        TargetAmount targetAmount = targetAmountService.readTargetAmount(targetAmountId)
                .filter(TargetAmount::isAllocatedAmount)
                .orElseThrow(() -> new TargetAmountErrorException(TargetAmountErrorCode.NOT_FOUND_TARGET_AMOUNT));

        if (!targetAmount.isThatMonth()) {
            throw new TargetAmountErrorException(TargetAmountErrorCode.INVALID_TARGET_AMOUNT_DATE);
        }

        targetAmountService.deleteTargetAmount(targetAmount);
    }
}
