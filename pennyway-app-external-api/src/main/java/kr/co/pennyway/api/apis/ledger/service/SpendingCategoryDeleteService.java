package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.domain.context.finance.service.SpendingCategoryService;
import kr.co.pennyway.domain.context.finance.service.SpendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingCategoryDeleteService {
    private final SpendingCategoryService spendingCategoryService;
    private final SpendingService spendingService;

    @Transactional
    public void execute(Long categoryId) {
        spendingService.deleteSpendingsByCategoryId(categoryId);
        spendingCategoryService.deleteSpendingCustomCategory(categoryId);
    }
}
