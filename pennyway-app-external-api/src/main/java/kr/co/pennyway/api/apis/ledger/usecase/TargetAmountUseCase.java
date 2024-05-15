package kr.co.pennyway.api.apis.ledger.usecase;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.api.apis.ledger.mapper.TargetAmountMapper;
import kr.co.pennyway.api.apis.ledger.service.TargetAmountSaveService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorCode;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorException;
import kr.co.pennyway.domain.domains.target.service.TargetAmountService;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
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

    @Transactional
    public void updateTargetAmount(Long userId, LocalDate date, Integer amount) {
        targetAmountSaveService.saveTargetAmount(userId, date, amount);
    }

    @Transactional(readOnly = true)
    public TargetAmountDto.WithTotalSpendingRes getTargetAmountAndTotalSpending(Long userId, LocalDate date) {
        Optional<TargetAmount> targetAmount = targetAmountService.readTargetAmountThatMonth(userId, date);
        Optional<TotalSpendingAmount> totalSpending = spendingService.readTotalSpendingAmountByUserId(userId, date);

        return TargetAmountMapper.toWithTotalSpendingResponse(targetAmount.orElse(null), totalSpending.orElse(null), date);
    }

    @Transactional(readOnly = true)
    public List<TargetAmountDto.WithTotalSpendingRes> getTargetAmountsAndTotalSpendings(Long userId, LocalDate date) {
        User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));

        List<TargetAmount> targetAmounts = targetAmountService.readTargetAmountsByUserId(userId);
        List<TotalSpendingAmount> totalSpendings = spendingService.readTotalSpendingsAmountByUserId(userId);

        return TargetAmountMapper.toWithTotalSpendingResponses(targetAmounts, totalSpendings, user.getCreatedAt().toLocalDate(), date);
    }
  
    @Transactional
    public void deleteTargetAmount(Long userId, LocalDate date) {
        TargetAmount targetAmount = targetAmountService.readTargetAmountThatMonth(userId, date)
                .filter(TargetAmount::isAllocatedAmount)
                .orElseThrow(() -> new TargetAmountErrorException(TargetAmountErrorCode.NOT_FOUND_TARGET_AMOUNT));

        targetAmountService.deleteTargetAmount(targetAmount);
    }
}
