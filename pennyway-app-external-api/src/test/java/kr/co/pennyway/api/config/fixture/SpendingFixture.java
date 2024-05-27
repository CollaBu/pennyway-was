package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import kr.co.pennyway.domain.domains.user.domain.User;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SpendingFixture {
    private static final String SPENDING_TABLE = "spending";

    public static Spending toSpending(User user) {
        return Spending.builder()
                .amount(10000)
                .category(SpendingCategory.FOOD)
                .spendAt(LocalDateTime.now())
                .accountName("카페인 수혈")
                .memo("아메리카노 1잔")
                .user(user)
                .build();
    }

    public static void bulkInsertSpending(User user, int capacity, NamedParameterJdbcTemplate jdbcTemplate) {
        Collection<Spending> spendings = getRandomSpendings(user, capacity);

        String sql = String.format("""
                INSERT INTO `%s` (amount, category, spend_at, account_name, memo, user_id, spending_custom_category_id, created_at, updated_at, deleted_at)
                VALUES (:amount, 1+FLOOR(RAND()*11), :spendAt, :accountName, :memo, :user.id, null, NOW(), NOW(), null)
                """, SPENDING_TABLE);
        SqlParameterSource[] params = spendings.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);
        jdbcTemplate.batchUpdate(sql, params);
    }

    private static List<Spending> getRandomSpendings(User user, int capacity) {
        List<Spending> spending = new ArrayList<>(capacity);

        for (int i = 0; i < 100; i++) {
            spending.add(Spending.builder()
                    .amount(ThreadLocalRandom.current().nextInt(100, 10000001))
                    .category(SpendingCategory.FOOD)
                    .spendAt(getRandomSpendAt(user))
                    .accountName(getRandomAccountName())
                    .memo((i % 5 == 0) ? "메모" : null)
                    .user(user)
                    .spendingCustomCategory(null)
                    .build()
            );
        }

        return spending;
    }

    private static LocalDateTime getRandomSpendAt(User user) {
        LocalDate startAt = user.getCreatedAt().toLocalDate();
        LocalDate endAt = LocalDate.now();

        int year = ThreadLocalRandom.current().nextInt(startAt.getYear(), endAt.getYear() + 1);
        int month = (year == endAt.getYear()) ? ThreadLocalRandom.current().nextInt(1, endAt.getMonthValue() + 1) : ThreadLocalRandom.current().nextInt(1, 13);
        int day = ThreadLocalRandom.current().nextInt(1, 29);

        return LocalDateTime.of(year, month, day, 0, 0, 0);
    }

    private static String getRandomAccountName() {
        List<String> accountNames = List.of("현금", "카드", "통장", "월급통장", "적금", "보험", "투자", "기타");
        return accountNames.get(ThreadLocalRandom.current().nextInt(0, accountNames.size()));
    }
}
