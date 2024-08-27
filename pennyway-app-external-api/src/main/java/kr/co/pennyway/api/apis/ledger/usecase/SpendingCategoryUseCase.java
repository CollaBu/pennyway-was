package kr.co.pennyway.api.apis.ledger.usecase;

import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.api.apis.ledger.mapper.SpendingCategoryMapper;
import kr.co.pennyway.api.apis.ledger.mapper.SpendingMapper;
import kr.co.pennyway.api.apis.ledger.service.*;
import kr.co.pennyway.api.common.query.SpendingCategoryType;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SpendingCategoryUseCase {
    private final SpendingCategorySaveService spendingCategorySaveService;
    private final SpendingCategorySearchService spendingCategorySearchService;
    private final SpendingCategoryDeleteService spendingCategoryDeleteService;

    private final SpendingSearchService spendingSearchService;
    private final SpendingUpdateService spendingUpdateService;

    @Transactional
    public SpendingCategoryDto.Res createSpendingCategory(Long userId, String categoryName, SpendingCategory icon) {
        SpendingCustomCategory category = spendingCategorySaveService.create(userId, categoryName, icon);

        return SpendingCategoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<SpendingCategoryDto.Res> getSpendingCategories(Long userId) {
        List<SpendingCustomCategory> categories = spendingCategorySearchService.readSpendingCustomCategories(userId);

        return SpendingCategoryMapper.toResponses(categories);
    }

    @Transactional
    public void deleteSpendingCategory(Long categoryId) {
        spendingCategoryDeleteService.execute(categoryId);
    }

    @Transactional(readOnly = true)
    public int getSpendingTotalCountByCategory(Long userId, Long categoryId, SpendingCategoryType type) {
        return spendingSearchService.readSpendingTotalCountByCategoryId(userId, categoryId, type);
    }

    @Transactional(readOnly = true)
    public SpendingSearchRes.MonthSlice getSpendingsByCategory(Long userId, Long categoryId, Pageable pageable, SpendingCategoryType type) {
        Slice<Spending> spendings = spendingSearchService.readSpendingsByCategoryId(userId, categoryId, pageable, type);

        return SpendingMapper.toMonthSlice(spendings);
    }

    @Transactional
    public SpendingCategoryDto.Res updateSpendingCategory(Long categoryId, String name, SpendingCategory icon) {
        SpendingCustomCategory category = spendingCategorySaveService.update(categoryId, name, icon);

        return SpendingCategoryMapper.toResponse(category);
    }

    @Transactional
    public void migrateSpendingsByCategory(Long fromId, SpendingCategoryType fromType, Long toId, SpendingCategoryType toType, Long userId) {
        spendingUpdateService.migrateSpendings(fromId, fromType, toId, toType, userId);
    }
}
