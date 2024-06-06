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
import java.util.concurrent.ThreadLocalRandom;

public enum SpendingCustomCategoryFixture {
    GENERAL_SPENDING_CUSTOM_CATEGORY("커스텀 지출 내역 카테고리", SpendingCategory.FOOD);

    private final String name;
    private final SpendingCategory icon;

    SpendingCustomCategoryFixture(String name, SpendingCategory icon) {
        this.name = name;
        this.icon = icon;
    }

    public static void bulkInsertCustomCategory(User user, int capacity, NamedParameterJdbcTemplate jdbcTemplate) {
        Collection<SpendingCustomCategory> customCategories = getRandomCustomCategories(user, capacity);

        String sql = String.format("""
                INSERT INTO `%s` (name, icon, user_id, created_at, updated_at, deleted_at)
                VALUES (:name, 1, :user.id, NOW(), NOW(), null)
                """, "spending_custom_category");
        SqlParameterSource[] params = customCategories.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);
        jdbcTemplate.batchUpdate(sql, params);
    }

    private static List<SpendingCustomCategory> getRandomCustomCategories(User user, int capacity) {
        List<SpendingCustomCategory> customCategories = new ArrayList<>(capacity);

        for (int i = 0; i < capacity; i++) {
            customCategories.add(SpendingCustomCategory.of(
                    getRandomCustomCategoryName(),
                    SpendingCategory.OTHER,
                    user
            ));
        }
        return customCategories;
    }

    private static String getRandomCustomCategoryName() {
        List<String> customCategoryNames = List.of("은밀한 취미", "특이한 취미", "은밀한 지출", "특이한 지출", "은밀한 비용");
        return customCategoryNames.get(ThreadLocalRandom.current().nextInt(0, customCategoryNames.size()));
    }

    public SpendingCustomCategory toCustomSpendingCategory(User user) {
        return SpendingCustomCategory.of(name, icon, user);
    }
}
