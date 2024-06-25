package kr.co.pennyway.api.apis.ledger.usecase;

import kr.co.pennyway.api.apis.ledger.dto.SpendingReq;
import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.api.apis.ledger.mapper.SpendingMapper;
import kr.co.pennyway.api.apis.ledger.service.SpendingDeleteService;
import kr.co.pennyway.api.apis.ledger.service.SpendingSaveService;
import kr.co.pennyway.api.apis.ledger.service.SpendingSearchService;
import kr.co.pennyway.api.apis.ledger.service.SpendingUpdateService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SpendingUseCase {
    private final SpendingSaveService spendingSaveService;
    private final SpendingSearchService spendingSearchService;
    private final SpendingUpdateService spendingUpdateService;
    private final SpendingDeleteService spendingDeleteService;

    @Transactional
    public SpendingSearchRes.Individual createSpending(Long userId, SpendingReq request) {
        Spending spending = spendingSaveService.createSpending(userId, request);

        return SpendingMapper.toSpendingSearchResIndividual(spending);
    }

    @Transactional(readOnly = true)
    public SpendingSearchRes.Month getSpendingsAtYearAndMonth(Long userId, int year, int month) {
        List<Spending> spendings = spendingSearchService.readSpendingsAtYearAndMonth(userId, year, month);

        return SpendingMapper.toSpendingSearchResMonth(spendings, year, month);
    }

    @Transactional(readOnly = true)
    public SpendingSearchRes.Individual getSpedingDetail(Long spendingId) {
        Spending spending = spendingSearchService.readSpending(spendingId);

        return SpendingMapper.toSpendingSearchResIndividual(spending);
    }

    @Transactional
    public SpendingSearchRes.Individual updateSpending(Long spendingId, SpendingReq request) {
        Spending updatedSpending = spendingUpdateService.updateSpending(spendingId, request);

        return SpendingMapper.toSpendingSearchResIndividual(updatedSpending);
    }

    public void deleteSpending(Long spendingId) {
        spendingDeleteService.deleteSpending(spendingId);
    }
}
