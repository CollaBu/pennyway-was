package kr.co.pennyway.api.apis.ledge.usecase;

import kr.co.pennyway.api.apis.ledge.service.TargetAmountSaveService;
import kr.co.pennyway.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class TargetAmountUseCase {
    private final TargetAmountSaveService targetAmountSaveService;

    @Transactional
    public void updateTargetAmount(Long userId, LocalDate date, Integer amount) {
        targetAmountSaveService.saveTargetAmount(userId, date, amount);
    }
}
