package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.api.apis.ledger.mapper.TargetAmountMapper;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorCode;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorException;
import kr.co.pennyway.domain.domains.target.service.TargetAmountService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TargetAmountSearchService {
    private final UserService userService;
    private final SpendingService spendingService;
    private final TargetAmountService targetAmountService;

    @Transactional(readOnly = true)
    public List<TargetAmountDto.WithTotalSpendingRes> readTargetAmountsAndTotalSpendings(Long userId, LocalDate date) {
        User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));

        List<TargetAmount> targetAmounts = targetAmountService.readTargetAmountsByUserId(userId);
        List<TotalSpendingAmount> totalSpendings = spendingService.readTotalSpendingsAmountByUserId(userId);

        return TargetAmountMapper.toWithTotalSpendingResponses(targetAmounts, totalSpendings, user.getCreatedAt().toLocalDate(), date);
    }

    @Transactional(readOnly = true)
    public TargetAmount readTargetAmountThatMonth(Long userId, LocalDate date) {
        return targetAmountService.readTargetAmountThatMonth(userId, date).orElseThrow(() -> new TargetAmountErrorException(TargetAmountErrorCode.NOT_FOUND_TARGET_AMOUNT));
    }

    @Transactional(readOnly = true)
    public Integer readRecentTargetAmount(Long userId) {
        return targetAmountService.readRecentTargetAmount(userId)
                .map(TargetAmount::getAmount)
                .orElse(-1);
    }
}
