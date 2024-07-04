package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import kr.co.pennyway.domain.domains.user.domain.User;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum SpendingCustomCategoryFixture {
    GENERAL_SPENDING_CUSTOM_CATEGORY("일반카테고리", SpendingCategory.FOOD);

    private final String name;
    private final SpendingCategory icon;

    SpendingCustomCategoryFixture(String name, SpendingCategory icon) {
        this.name = name;
        this.icon = icon;
    }

    public static void bulkInsertCustomCategory(User user, int capacity, NamedParameterJdbcTemplate jdbcTemplate) {
        Collection<SpendingCustomCategory> customCategories = getCustomCategories(user, capacity);

        String sql = String.format("""
                INSERT INTO `%s` (name, icon, user_id, created_at, updated_at, deleted_at)
                VALUES (:name, 1, :user.id, NOW(), NOW(), null)
                """, "spending_custom_category");
        SqlParameterSource[] params = customCategories.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);
        jdbcTemplate.batchUpdate(sql, params);
    }

    private static List<SpendingCustomCategory> getCustomCategories(User user, int capacity) {
        List<SpendingCustomCategory> customCategories = new ArrayList<>(capacity);

        for (int i = 0; i < capacity; i++) {
            customCategories.add(SpendingCustomCategoryFixture.GENERAL_SPENDING_CUSTOM_CATEGORY.toCustomSpendingCategory(user));
        }
        return customCategories;
    }

    public SpendingCustomCategory toCustomSpendingCategory(User user) {
        return SpendingCustomCategory.of(name, icon, user);
    }
}
