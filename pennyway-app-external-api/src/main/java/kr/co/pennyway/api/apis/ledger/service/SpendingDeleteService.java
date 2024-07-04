package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorCode;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorException;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingDeleteService {
    private final SpendingService spendingService;

    @Transactional
    public void deleteSpending(Long spendingId) {
        Spending spending = spendingService.readSpending(spendingId)
                .orElseThrow(() -> new SpendingErrorException(SpendingErrorCode.NOT_FOUND_SPENDING));

        spendingService.deleteSpending(spending);
    }

    @Transactional
    public void deleteSpendings(List<Long> spendingIds) {
        spendingService.deleteSpendingsInQuery(spendingIds);
    }
}
