package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.domain.domains.spending.service.SpendingCustomCategoryService;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingCategoryDeleteService {
    private final SpendingCustomCategoryService spendingCustomCategoryService;
    private final SpendingService spendingService;

    @Transactional
    public void execute(Long categoryId) {
        spendingService.deleteSpendingsByCategoryIdInQuery(categoryId);
        spendingCustomCategoryService.deleteSpendingCustomCategory(categoryId);
    }
}
