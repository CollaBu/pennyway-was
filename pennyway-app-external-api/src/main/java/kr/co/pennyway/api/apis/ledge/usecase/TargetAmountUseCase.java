package kr.co.pennyway.api.apis.ledge.usecase;

import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.target.service.TargetAmountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class TargetAmountUseCase {
    private TargetAmountService targetAmountService;

    @Transactional
    public void updateTargetAmount(Long userId, LocalDate date, Integer amount) {
    }
}
