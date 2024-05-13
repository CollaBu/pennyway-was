package kr.co.pennyway.api.apis.ledger.usecase;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.api.apis.ledger.service.TargetAmountSaveService;
import kr.co.pennyway.common.annotation.UseCase;
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

    @Transactional
    public void updateTargetAmount(Long userId, LocalDate date, Integer amount) {
        targetAmountSaveService.saveTargetAmount(userId, date, amount);
    }

    @Transactional(readOnly = true)
    public TargetAmountDto.GetParamReq getTargetAmountAndTotalSpending(LocalDate date) {
        return null;
    }

    @Transactional(readOnly = true)
    public List<TargetAmountDto.GetParamReq> getTargetAmountsAndTotalSpendings(LocalDate date) {
        return null;
    }
}
