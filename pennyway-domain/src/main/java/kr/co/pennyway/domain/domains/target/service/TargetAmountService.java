package kr.co.pennyway.domain.domains.target.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.repository.TargetAmountRepository;
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
    private final TargetAmountRepository targetAmountRepository;

    @Transactional
    public TargetAmount createTargetAmount(TargetAmount targetAmount) {
        return targetAmountRepository.save(targetAmount);
    }

    @Transactional(readOnly = true)
    public Optional<TargetAmount> readTargetAmountThatMonth(Long userId, LocalDate date) {
        return targetAmountRepository.findByUserIdThatMonth(userId, date);
    }

    @Transactional(readOnly = true)
    public List<TargetAmount> readTargetAmountsByUserId(Long userId) {
        return targetAmountRepository.findByUser_Id(userId);
    }
}
