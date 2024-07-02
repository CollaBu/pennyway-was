package kr.co.pennyway.api.apis.ledger.usecase;

import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.api.apis.ledger.mapper.SpendingCategoryMapper;
import kr.co.pennyway.api.apis.ledger.mapper.SpendingMapper;
import kr.co.pennyway.api.apis.ledger.service.SpendingCategorySaveService;
import kr.co.pennyway.api.apis.ledger.service.SpendingCategorySearchService;
import kr.co.pennyway.api.apis.ledger.service.SpendingSearchService;
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

    private final SpendingSearchService spendingSearchService;

    @Transactional
    public SpendingCategoryDto.Res createSpendingCategory(Long userId, String categoryName, SpendingCategory icon) {
        SpendingCustomCategory category = spendingCategorySaveService.execute(userId, categoryName, icon);

        return SpendingCategoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<SpendingCategoryDto.Res> getSpendingCategories(Long userId) {
        List<SpendingCustomCategory> categories = spendingCategorySearchService.readSpendingCustomCategories(userId);

        return SpendingCategoryMapper.toResponses(categories);
    }

    @Transactional(readOnly = true)
    public SpendingSearchRes.MonthSlice getSpendingsByCategory(Long userId, Long categoryId, Pageable pageable, SpendingCategoryType type) {
        Slice<Spending> spendings = spendingSearchService.readSpendingsByCategoryId(userId, categoryId, pageable, type);

        return SpendingMapper.toMonthSlice(spendings);
    }
}
