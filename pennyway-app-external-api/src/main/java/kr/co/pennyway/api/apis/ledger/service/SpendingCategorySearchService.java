package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.service.SpendingCustomCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingCategorySearchService {
    private final SpendingCustomCategoryService spendingCustomCategoryService;

    @Transactional(readOnly = true)
    public List<SpendingCustomCategory> readSpendingCustomCategories(Long userId) {
        return spendingCustomCategoryService.readSpendingCustomCategories(userId);
    }
}
