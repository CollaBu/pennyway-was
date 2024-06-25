package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingSearchService {
    private final SpendingService spendingService;

    @Transactional(readOnly = true)
    public Optional<TotalSpendingAmount> readTotalSpendingAmountByUserIdThatMonth(Long userId, LocalDate date) {
        return spendingService.readTotalSpendingAmountByUserId(userId, date);
    }

    @Transactional(readOnly = true)
    public List<TotalSpendingAmount> readTotalSpendingsAmountByUserId(Long userId) {
        return spendingService.readTotalSpendingsAmountByUserId(userId);
    }
}
