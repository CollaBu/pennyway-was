package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorCode;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorException;
import kr.co.pennyway.domain.domains.target.service.TargetAmountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TargetAmountDeleteService {
    private final TargetAmountService targetAmountService;

    @Transactional
    public void execute(Long targetAmountId) {
        TargetAmount targetAmount = targetAmountService.readTargetAmount(targetAmountId)
                .orElseThrow(() -> new TargetAmountErrorException(TargetAmountErrorCode.NOT_FOUND_TARGET_AMOUNT));

        if (!targetAmount.isThatMonth()) {
            throw new TargetAmountErrorException(TargetAmountErrorCode.INVALID_TARGET_AMOUNT_DATE);
        }

        targetAmountService.deleteTargetAmount(targetAmount);
    }
}
