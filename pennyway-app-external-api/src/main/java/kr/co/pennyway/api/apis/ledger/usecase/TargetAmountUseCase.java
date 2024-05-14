package kr.co.pennyway.api.apis.ledger.usecase;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.api.apis.ledger.mapper.TargetAmountMapper;
import kr.co.pennyway.api.apis.ledger.service.SpendingSearchService;
import kr.co.pennyway.api.apis.ledger.service.TargetAmountSaveService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.service.TargetAmountService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
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
    public TargetAmountDto.WithTotalSpendingRes getTargetAmountAndTotalSpending(Long userId, LocalDate date) {
        Optional<TargetAmount> targetAmount = targetAmountService.readTargetAmountThatMonth(userId, date); // TODO: null 처리 다른 방식으로 해결
        Optional<TotalSpendingAmount> totalSpending = spendingService.readTotalSpendingAmountByUserId(userId, date);

        log.info("targetAmount {}", targetAmount);
        log.info("totalSpending {}", totalSpending);

        return TargetAmountMapper.toWithTotalSpendingRes(targetAmount, totalSpending, date);
    }

    @Transactional(readOnly = true)
    public List<TargetAmountDto.WithTotalSpendingRes> getTargetAmountsAndTotalSpendings(Long userId, LocalDate date) {
        User user = userService.readUser(userId).orElseThrow(
                () -> new UserErrorException(UserErrorCode.NOT_FOUND)
        );

        List<TargetAmount> targetAmounts = targetAmountService.readTargetAmountsByUserId(userId);
        List<TotalSpendingAmount> totalSpendings = spendingService.readTotalSpendingsAmountByUserId(userId);

        for (TotalSpendingAmount spending : totalSpendings) {
            log.info("{}", spending);
        }

        return TargetAmountMapper.toWithTotalSpendingsRes(targetAmounts, totalSpendings, user.getCreatedAt().toLocalDate(), date);
    }
}
