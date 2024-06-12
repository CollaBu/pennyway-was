package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.api.apis.ledger.mapper.TargetAmountMapper;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorCode;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorException;
import kr.co.pennyway.domain.domains.target.service.TargetAmountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TargetAmountSearchService {
    private final SpendingService spendingService;
    private final TargetAmountService targetAmountService;

    @Transactional(readOnly = true)
    public TargetAmountDto.WithTotalSpendingRes readTargetAmountAndTotalSpending(Long userId, LocalDate date) {
        TargetAmount targetAmount = targetAmountService.readTargetAmountThatMonth(userId, date).orElseThrow(() -> new TargetAmountErrorException(TargetAmountErrorCode.NOT_FOUND_TARGET_AMOUNT));
        Optional<TotalSpendingAmount> totalSpending = spendingService.readTotalSpendingAmountByUserId(userId, date);

        return TargetAmountMapper.toWithTotalSpendingResponse(targetAmount, totalSpending.orElse(null), date);
    }

    @Transactional(readOnly = true)
    public Integer readRecentTargetAmount(Long userId) {
        return targetAmountService.readRecentTargetAmount(userId)
                .map(TargetAmount::getAmount)
                .orElse(-1);
    }
}
