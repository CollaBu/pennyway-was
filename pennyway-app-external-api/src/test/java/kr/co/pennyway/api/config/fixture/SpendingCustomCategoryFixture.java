package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import kr.co.pennyway.domain.domains.user.domain.User;

public enum SpendingCustomCategoryFixture {
    GENERAL_SPENDING_CUSTOM_CATEGORY("일반카테고리", SpendingCategory.FOOD);

    private final String name;
    private final SpendingCategory icon;

    SpendingCustomCategoryFixture(String name, SpendingCategory icon) {
        this.name = name;
        this.icon = icon;
    }

    public SpendingCustomCategory toCustomSpendingCategory(User user) {
        return SpendingCustomCategory.of(name, icon, user);
    }
}
