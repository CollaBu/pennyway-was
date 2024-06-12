package kr.co.pennyway.api.apis.ledger.usecase;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.api.apis.ledger.mapper.TargetAmountMapper;
import kr.co.pennyway.api.apis.ledger.service.TargetAmountDeleteService;
import kr.co.pennyway.api.apis.ledger.service.TargetAmountSaveService;
import kr.co.pennyway.api.apis.ledger.service.TargetAmountSearchService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.common.redisson.DistributedLockPrefix;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class TargetAmountUseCase {
    private final TargetAmountSaveService targetAmountSaveService;
    private final TargetAmountSearchService targetAmountSearchService;
    private final TargetAmountDeleteService targetAmountDeleteService;

    @Transactional
    public TargetAmountDto.TargetAmountInfo createTargetAmount(Long userId, int year, int month) {
        TargetAmount targetAmount = targetAmountSaveService.createTargetAmount(DistributedLockPrefix.TARGET_AMOUNT_USER, userId, LocalDate.of(year, month, 1));

        return TargetAmountDto.TargetAmountInfo.from(targetAmount);
    }

    public TargetAmountDto.WithTotalSpendingRes getTargetAmountAndTotalSpending(Long userId, LocalDate date) {
        return targetAmountSearchService.readTargetAmountAndTotalSpending(userId, date);
    }

    public List<TargetAmountDto.WithTotalSpendingRes> getTargetAmountsAndTotalSpendings(Long userId, LocalDate date) {
        return targetAmountSearchService.readTargetAmountsAndTotalSpendings(userId, date);
    }

    public TargetAmountDto.RecentTargetAmountRes getRecentTargetAmount(Long userId) {
        return TargetAmountMapper.toRecentTargetAmountResponse(targetAmountSearchService.readRecentTargetAmount(userId));
    }

    @Transactional
    public TargetAmountDto.TargetAmountInfo updateTargetAmount(Long targetAmountId, Integer amount) {
        TargetAmount targetAmount = targetAmountSaveService.updateTargetAmount(targetAmountId, amount);

        return TargetAmountDto.TargetAmountInfo.from(targetAmount);
    }

    public void deleteTargetAmount(Long targetAmountId) {
        targetAmountDeleteService.execute(targetAmountId);
    }
}
