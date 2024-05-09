package kr.co.pennyway.api.apis.ledger.usecase;

import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.dto.CategoryInfo;
import kr.co.pennyway.domain.domains.spending.service.SpendingCustomCategoryService;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SpendingCategoryUseCase {
    private final UserService userService;
    private final SpendingCustomCategoryService spendingCustomCategoryService;

    @Transactional
    public SpendingCategoryDto.Res createSpendingCategory(Long userId, String categoryName, SpendingCategory icon) {
        User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));

        SpendingCustomCategory category = spendingCustomCategoryService.createSpendingCustomCategory(SpendingCustomCategory.of(categoryName, icon, user));

        return SpendingCategoryDto.Res.from(CategoryInfo.of(category.getId(), category.getName(), category.getIcon()));
    }
}
