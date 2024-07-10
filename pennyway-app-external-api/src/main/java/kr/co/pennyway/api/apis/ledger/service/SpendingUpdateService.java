package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.api.apis.ledger.dto.SpendingReq;
import kr.co.pennyway.api.common.query.SpendingCategoryType;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorCode;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorException;
import kr.co.pennyway.domain.domains.spending.service.SpendingCustomCategoryService;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingUpdateService {
    private final SpendingService spendingService;
    private final SpendingCustomCategoryService spendingCustomCategoryService;

    @Transactional
    public Spending updateSpending(Long spendingId, SpendingReq request) {
        Spending spending = spendingService.readSpending(spendingId).orElseThrow(() -> new SpendingErrorException(SpendingErrorCode.NOT_FOUND_SPENDING));

        SpendingCustomCategory customCategory = (request.isCustomCategory())
                ? spendingCustomCategoryService.readSpendingCustomCategory(request.categoryId()).orElseThrow(() -> new SpendingErrorException(SpendingErrorCode.NOT_FOUND_CUSTOM_CATEGORY))
                : null;

        spending.update(request.amount(), request.icon(), request.spendAt().atStartOfDay(), request.accountName(), request.memo(), customCategory);

        return spending;
    }

    @Transactional
    public void migrateSpendings(Long fromCategoryId, Long toCategoryId, SpendingCategoryType toType) {
        if (toType.equals(SpendingCategoryType.CUSTOM)) {
            spendingService.migrateSpendingsByCategoryId(fromCategoryId, toCategoryId);
        } else {
            SpendingCategory spendingCategory = SpendingCategory.fromCode(toCategoryId.toString());
            spendingService.migrateSpendingsByCategory(fromCategoryId, spendingCategory);
        }
    }
}
