package kr.co.pennyway.domain.domains.target.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.repository.TargetAmountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class TargetAmountService {
    private final TargetAmountRepository targetAmountRepository;

    @Transactional
    public TargetAmount save(TargetAmount targetAmount) {
        return targetAmountRepository.save(targetAmount);
    }

    @Transactional(readOnly = true)
    public Optional<TargetAmount> findTargetAmountThatMonth(Long userId, LocalDate date) {
        return targetAmountRepository.findByUserIdAndDateThatMonth(userId, date);
    }
}
