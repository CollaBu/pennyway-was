package kr.co.pennyway.api.apis.ledger.usecase;

import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.api.apis.ledger.mapper.SpendingCategoryMapper;
import kr.co.pennyway.api.apis.ledger.service.SpendingCategorySaveService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.dto.CategoryInfo;
import kr.co.pennyway.domain.domains.spending.service.SpendingCustomCategoryService;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SpendingCategoryUseCase {
    private final UserService userService;
    private final SpendingCustomCategoryService spendingCustomCategoryService;

    private final SpendingCategorySaveService spendingCategorySaveService;

    @Transactional
    public SpendingCategoryDto.Res createSpendingCategory(Long userId, String categoryName, SpendingCategory icon) {
        SpendingCustomCategory category = spendingCategorySaveService.execute(userId, categoryName, icon);

        return SpendingCategoryMapper.toRes(category);
    }

    @Transactional(readOnly = true)
    public List<SpendingCategoryDto.Res> getSpendingCategories(Long userId) {
        List<SpendingCustomCategory> categories = spendingCustomCategoryService.readSpendingCustomCategories(userId);

        return categories.stream()
                .map(category -> SpendingCategoryDto.Res.from(CategoryInfo.of(category.getId(), category.getName(), category.getIcon())))
                .toList();
    }
}
