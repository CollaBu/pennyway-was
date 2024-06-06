package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.api.apis.ledger.dto.SpendingReq;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorCode;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorException;
import kr.co.pennyway.domain.domains.spending.service.SpendingCustomCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingUpdateService {
    private final SpendingCustomCategoryService spendingCustomCategoryService;

    @Transactional
    public Spending updateSpending(Spending spending, SpendingReq request) {
        SpendingCustomCategory customCategory = (request.isCustomCategory())
                ? spendingCustomCategoryService.readSpendingCustomCategory(request.categoryId()).orElseThrow(() -> new SpendingErrorException(SpendingErrorCode.NOT_FOUND_CUSTOM_CATEGORY))
                : null;

        spending.update(request.amount(), request.icon(), request.spendAt().atStartOfDay(), request.accountName(), request.memo(), customCategory);

        return spending;
    }
}
