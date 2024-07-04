package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.api.apis.ledger.dto.SpendingReq;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
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

public enum SpendingFixture {
    GENERAL_SPENDING(10000, SpendingCategory.FOOD, LocalDateTime.now(), "카페인 수혈", "아메리카노 1잔"),
    CUSTOM_CATEGORY_SPENDING(10000, SpendingCategory.CUSTOM, LocalDateTime.now(), "커스텀 카페인 수혈", "아메리카노 1잔");

    private final int amount;
    private final SpendingCategory category;
    private final LocalDateTime spendAt;
    private final String accountName;
    private final String memo;


    SpendingFixture(int amount, SpendingCategory category, LocalDateTime spendAt, String accountName, String memo) {
        this.amount = amount;
        this.category = category;
        this.spendAt = spendAt;
        this.accountName = accountName;
        this.memo = memo;
    }

    public static SpendingReq toSpendingReq(User user) {
        return new SpendingReq(10000, -1L, SpendingCategory.FOOD, LocalDate.now(), "카페인 수혈", "아메리카노 1잔");
    }

    /**
     * Spending 객체들을 벌크연산으로 삽입한다.
     *
     * @param user             {@link User} Spending 객체의 사용자
     * @param capacity         {@link Integer} 생성할 Spending 객체의 개수
     * @param customCategoryId {@link Long} Spending 객체의 customCategoryId. 기본 카테고리를 가질시 0이 된다.
     * @param jdbcTemplate     {@link NamedParameterJdbcTemplate} Spending 객체를 생성할 때 사용할 jdbcTemplate
     */
    public static void bulkInsertSpending(User user, int capacity, Long customCategoryId, NamedParameterJdbcTemplate jdbcTemplate) {
        Collection<Spending> spendings = getRandomSpendings(user, capacity);
        String sql;
        if (!customCategoryId.equals(0L)) {
            sql = String.format("""
                    INSERT INTO `%s` (amount, category, spend_at, account_name, memo, user_id, spending_custom_category_id, created_at, updated_at, deleted_at)
                    VALUES (:amount, 1+FLOOR(RAND()*11), :spendAt, :accountName, :memo, :user.id, %d, NOW(), NOW(), null)
                    """, "spending", customCategoryId);
        } else {
            sql = String.format("""
                    INSERT INTO `%s` (amount, category, spend_at, account_name, memo, user_id, spending_custom_category_id, created_at, updated_at, deleted_at)
                    VALUES (:amount, 1+FLOOR(RAND()*11), :spendAt, :accountName, :memo, :user.id, null, NOW(), NOW(), null)
                    """, "spending");
        }

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

    public Spending toSpending(User user) {
        return Spending.builder()
                .amount(amount)
                .category(category)
                .spendAt(spendAt)
                .accountName(accountName)
                .memo(memo)
                .user(user)
                .build();
    }

    public Spending toCustomCategorySpending(User user, SpendingCustomCategory customCategory) {
        return Spending.builder()
                .amount(amount)
                .category(category)
                .spendAt(spendAt)
                .accountName(accountName)
                .memo(memo)
                .user(user)
                .spendingCustomCategory(customCategory)
                .build();
    }
}