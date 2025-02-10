package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.domain.context.finance.service.SpendingCategoryService;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingCategorySearchService {
    private final SpendingCategoryService spendingCategoryService;

    @Transactional(readOnly = true)
    public List<SpendingCustomCategory> readSpendingCustomCategories(Long userId) {
        return spendingCategoryService.readSpendingCustomCategories(userId);
    }
}
