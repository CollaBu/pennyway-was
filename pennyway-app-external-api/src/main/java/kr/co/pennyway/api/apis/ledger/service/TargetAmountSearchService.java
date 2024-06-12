package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.service.TargetAmountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TargetAmountSearchService {
    private final TargetAmountService targetAmountService;

    @Transactional(readOnly = true)
    public Integer readRecentTargetAmount(Long userId) {
        return targetAmountService.readRecentTargetAmount(userId)
                .map(TargetAmount::getAmount)
                .orElse(-1);
    }
}
