package kr.co.pennyway.api.apis.ledger.usecase;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.api.apis.ledger.service.SpendingSearchService;
import kr.co.pennyway.api.apis.ledger.service.TargetAmountSaveService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
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
    private final SpendingSearchService spendingSearchService;

    @Transactional
    public void updateTargetAmount(Long userId, LocalDate date, Integer amount) {
        targetAmountSaveService.saveTargetAmount(userId, date, amount);
    }

    @Transactional(readOnly = true)
    public TargetAmountDto.GetResponse getTargetAmountAndTotalSpending(Long userId, LocalDate date) {
        TotalSpendingAmount totalSpending = spendingService.readTotalSpendingAmountByUserId(userId, date);
        log.info("{}", totalSpending);

        return null;
    }

    @Transactional(readOnly = true)
    public List<?> getTargetAmountsAndTotalSpendings(Long userId) {
        List<TotalSpendingAmount> totalSpending = spendingService.readTotalSpendingsAmountByUserId(userId);
        //

        for (TotalSpendingAmount spending : totalSpending) {
            log.info("{}", spending);
        }

        return totalSpending;
    }
}
