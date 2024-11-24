package kr.co.pennyway.domain.context.finance.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.service.SpendingCustomCategoryRdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class SpendingCategoryService {
    private final SpendingCustomCategoryRdbService spendingCustomCategoryRdbService;

    @Transactional
    public SpendingCustomCategory createSpendingCustomCategory(SpendingCustomCategory spendingCustomCategory) {
        return spendingCustomCategoryRdbService.createSpendingCustomCategory(spendingCustomCategory);
    }

    @Transactional(readOnly = true)
    public Optional<SpendingCustomCategory> readSpendingCustomCategory(Long id) {
        return spendingCustomCategoryRdbService.readSpendingCustomCategory(id);
    }

    @Transactional(readOnly = true)
    public List<SpendingCustomCategory> readSpendingCustomCategories(Long userId) {
        return spendingCustomCategoryRdbService.readSpendingCustomCategories(userId);
    }

    @Transactional(readOnly = true)
    public boolean isExistsSpendingCustomCategory(Long userId, Long categoryId) {
        return spendingCustomCategoryRdbService.isExistsSpendingCustomCategory(userId, categoryId);
    }

    @Transactional
    public void deleteSpendingCustomCategory(Long categoryId) {
        spendingCustomCategoryRdbService.deleteSpendingCustomCategory(categoryId);
    }

    @Transactional
    public void deleteSpendingCustomCategoriesByUserIdInQuery(Long userId) {
        spendingCustomCategoryRdbService.deleteSpendingCustomCategoriesByUserIdInQuery(userId);
    }
}
