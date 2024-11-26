package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.domain.context.account.service.UserService;
import kr.co.pennyway.domain.context.finance.service.SpendingCategoryService;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorCode;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorException;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingCategorySaveService {
    private final UserService userService;
    private final SpendingCategoryService spendingCategoryService;

    @Transactional
    public SpendingCustomCategory create(Long userId, String categoryName, SpendingCategory icon) {
        User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));

        return spendingCategoryService.createSpendingCustomCategory(SpendingCustomCategory.of(categoryName, icon, user));
    }

    @Transactional
    public SpendingCustomCategory update(Long categoryId, String name, SpendingCategory icon) {
        SpendingCustomCategory category = spendingCategoryService.readSpendingCustomCategory(categoryId)
                .orElseThrow(() -> new SpendingErrorException(SpendingErrorCode.NOT_FOUND_CUSTOM_CATEGORY));

        category.update(name, icon);
        return category;
    }
}
