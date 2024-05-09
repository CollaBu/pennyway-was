package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.api.apis.ledger.dto.SpendingReq;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorCode;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorException;
import kr.co.pennyway.domain.domains.spending.service.SpendingCustomCategoryService;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingSaveService {
    private final SpendingService spendingService;
    private final SpendingCustomCategoryService spendingCustomCategoryService;

    @Transactional
    public Spending createSpending(User user, SpendingReq request) {
        Spending spending;

        if (request.categoryId().equals(-1L)) {
            spending = spendingService.createSpending(request.toEntity(user));
        } else {
            SpendingCustomCategory customCategory = spendingCustomCategoryService.readSpendingCustomCategory(request.categoryId())
                    .orElseThrow(() -> new SpendingErrorException(SpendingErrorCode.NOT_FOUND_CUSTOM_CATEGORY));

            spending = spendingService.createSpending(request.toEntity(user, customCategory));
        }

        return spending;
    }
}
