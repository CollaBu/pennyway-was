package kr.co.pennyway.api.apis.ledger.usecase;

import kr.co.pennyway.api.apis.ledger.service.TargetAmountSaveService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorCode;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorException;
import kr.co.pennyway.domain.domains.target.service.TargetAmountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class TargetAmountUseCase {
    private final TargetAmountService targetAmountService;

    private final TargetAmountSaveService targetAmountSaveService;

    @Transactional
    public void updateTargetAmount(Long userId, LocalDate date, Integer amount) {
        targetAmountSaveService.saveTargetAmount(userId, date, amount);
    }

    @Transactional
    public void deleteTargetAmount(Long userId, LocalDate date) {
        TargetAmount targetAmount = targetAmountService.readTargetAmountThatMonth(userId, date)
                .filter(TargetAmount::isAllocatedAmount)
                .orElseThrow(() -> new TargetAmountErrorException(TargetAmountErrorCode.NOT_FOUND_TARGET_AMOUNT));

        targetAmountService.deleteTargetAmount(targetAmount);
    }
}
