package kr.co.pennyway.domain.context.finance.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.spending.service.SpendingRdbService;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class SpendingService {
    private final SpendingRdbService spendingRdbService;

    @Transactional
    public Spending createSpending(Spending spending) {
        return spendingRdbService.createSpending(spending);
    }

    @Transactional(readOnly = true)
    public Optional<Spending> readSpending(Long spendingId) {
        return spendingRdbService.readSpending(spendingId);
    }

    @Transactional(readOnly = true)
    public List<Spending> readSpendings(Long userId, int year, int month) {
        return spendingRdbService.readSpendings(userId, year, month);
    }

    @Transactional(readOnly = true)
    public int readSpendingTotalCountByCategoryId(Long userId, Long categoryId) {
        return spendingRdbService.readSpendingTotalCountByCategoryId(userId, categoryId);
    }

    @Transactional(readOnly = true)
    public int readSpendingTotalCountByCategory(Long userId, SpendingCategory spendingCategory) {
        return spendingRdbService.readSpendingTotalCountByCategory(userId, spendingCategory);
    }

    @Transactional(readOnly = true)
    public Slice<Spending> readSpendingsSliceByCategoryId(Long userId, Long categoryId, Pageable pageable) {
        return spendingRdbService.readSpendingsSliceByCategoryId(userId, categoryId, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<Spending> readSpendingsSliceByCategory(Long userId, SpendingCategory spendingCategory, Pageable pageable) {
        return spendingRdbService.readSpendingsSliceByCategory(userId, spendingCategory, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<TotalSpendingAmount> readTotalSpendingAmount(Long userId, LocalDate date) {
        return spendingRdbService.readTotalSpendingAmountByUserId(userId, date);
    }

    @Transactional(readOnly = true)
    public List<TotalSpendingAmount> readTotalSpendingsAmountByUserId(Long userId) {
        return spendingRdbService.readTotalSpendingsAmountByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean isExistsSpending(Long userId, Long spendingId) {
        return spendingRdbService.isExistsSpending(userId, spendingId);
    }

    @Transactional(readOnly = true)
    public long countByUserIdAndSpendingIds(Long userId, List<Long> spendingIds) {
        return spendingRdbService.countByUserIdAndIdIn(userId, spendingIds);
    }

    @Transactional
    public void updateCategoryByCustomCategory(SpendingCategory fromCategory, Long toId) {
        spendingRdbService.updateCategoryByCustomCategory(fromCategory, toId);
    }

    @Transactional
    public void updateCategoryByCategory(SpendingCategory fromCategory, SpendingCategory toCategory) {
        spendingRdbService.updateCategoryByCategory(fromCategory, toCategory);
    }

    @Transactional
    public void updateCustomCategoryByCustomCategory(Long fromId, Long toId) {
        spendingRdbService.updateCustomCategoryByCustomCategory(fromId, toId);
    }

    @Transactional
    public void updateCustomCategoryByCategory(Long fromId, SpendingCategory toCategory) {
        spendingRdbService.updateCustomCategoryByCategory(fromId, toCategory);
    }

    @Transactional
    public void deleteSpending(Spending spending) {
        spendingRdbService.deleteSpending(spending);
    }

    @Transactional
    public void deleteSpendings(List<Long> spendingIds) {
        spendingRdbService.deleteSpendingsInQuery(spendingIds);
    }

    @Transactional
    public void deleteSpendingsByUserId(Long userId) {
        spendingRdbService.deleteSpendingsByUserIdInQuery(userId);
    }

    @Transactional
    public void deleteSpendingsByCategoryId(Long categoryId) {
        spendingRdbService.deleteSpendingsByCategoryIdInQuery(categoryId);
    }
}
