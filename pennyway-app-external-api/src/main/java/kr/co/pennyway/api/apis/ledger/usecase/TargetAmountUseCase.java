package kr.co.pennyway.api.apis.ledger.usecase;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.api.apis.ledger.mapper.TargetAmountMapper;
import kr.co.pennyway.api.apis.ledger.service.SpendingSearchService;
import kr.co.pennyway.api.apis.ledger.service.TargetAmountDeleteService;
import kr.co.pennyway.api.apis.ledger.service.TargetAmountSaveService;
import kr.co.pennyway.api.apis.ledger.service.TargetAmountSearchService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.common.redisson.DistributedLockPrefix;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class TargetAmountUseCase {
    private final TargetAmountSaveService targetAmountSaveService;
    private final TargetAmountSearchService targetAmountSearchService;
    private final TargetAmountDeleteService targetAmountDeleteService;

    private final SpendingSearchService spendingSearchService;

    @Transactional
    public TargetAmountDto.TargetAmountInfo createTargetAmount(Long userId, int year, int month) {
        TargetAmount targetAmount = targetAmountSaveService.createTargetAmount(DistributedLockPrefix.TARGET_AMOUNT_USER, userId, LocalDate.of(year, month, 1));

        return TargetAmountDto.TargetAmountInfo.from(targetAmount);
    }

    @Transactional(readOnly = true)
    public TargetAmountDto.WithTotalSpendingRes getTargetAmountAndTotalSpending(Long userId, LocalDate date) {
        TargetAmount targetAmount = targetAmountSearchService.readTargetAmountThatMonth(userId, date);
        Optional<TotalSpendingAmount> totalSpending = spendingSearchService.readTotalSpendingAmountByUserIdThatMonth(userId, date);
        return TargetAmountMapper.toWithTotalSpendingResponse(targetAmount, totalSpending.orElse(null), date);
    }

    @Transactional(readOnly = true)
    public List<TargetAmountDto.WithTotalSpendingRes> getTargetAmountsAndTotalSpendings(Long userId, LocalDate date) {
        List<TargetAmount> targetAmounts = targetAmountSearchService.readTargetAmountsByUserId(userId);
        List<TotalSpendingAmount> totalSpendings = spendingSearchService.readTotalSpendingsAmountByUserId(userId);

        // 목표 금액 중 가장 오래된 날짜를 기준으로 잡는다. (마지막 원소)
        LocalDate startAt = targetAmounts.get(targetAmounts.size() - 1).getCreatedAt().toLocalDate();

        return TargetAmountMapper.toWithTotalSpendingResponses(targetAmounts, totalSpendings, date);
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
