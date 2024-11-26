package kr.co.pennyway.domain.context.finance.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.service.TargetAmountRdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class TargetAmountService {
    private final TargetAmountRdbService targetAmountRdbService;

    @Transactional
    public TargetAmount createTargetAmount(TargetAmount targetAmount) {
        return targetAmountRdbService.createTargetAmount(targetAmount);
    }

    @Transactional(readOnly = true)
    public Optional<TargetAmount> readTargetAmount(Long id) {
        return targetAmountRdbService.readTargetAmount(id);
    }

    @Transactional(readOnly = true)
    public Optional<TargetAmount> readTargetAmountThatMonth(Long userId, LocalDate date) {
        return targetAmountRdbService.readTargetAmountThatMonth(userId, date);
    }

    @Transactional(readOnly = true)
    public List<TargetAmount> readTargetAmountsByUserId(Long userId) {
        return targetAmountRdbService.readTargetAmountsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<TargetAmount> readRecentTargetAmount(Long userId) {
        return targetAmountRdbService.readRecentTargetAmount(userId);
    }

    @Transactional(readOnly = true)
    public boolean isExistsTargetAmountThatMonth(Long userId, LocalDate date) {
        return targetAmountRdbService.isExistsTargetAmountThatMonth(userId, date);
    }

    @Transactional(readOnly = true)
    public boolean isExistsTargetAmountByIdAndUserId(Long id, Long userId) {
        return targetAmountRdbService.isExistsTargetAmountByIdAndUserId(id, userId);
    }

    @Transactional
    public void deleteTargetAmount(TargetAmount targetAmount) {
        targetAmountRdbService.deleteTargetAmount(targetAmount);
    }
}
