package kr.co.pennyway.api.config.fixture;

import jakarta.persistence.EntityManager;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.user.domain.User;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public enum TargetAmountFixture {
    GENERAL_TARGET_AMOUNT(10000, true);

    private static final String TARGET_AMOUNT_TABLE = "target_amount";
    private final int amount;
    private final boolean isRead;

    TargetAmountFixture(int amount, boolean isRead) {
        this.amount = amount;
        this.isRead = isRead;
    }

    public static void bulkInsertTargetAmount(User user, NamedParameterJdbcTemplate jdbcTemplate) {
        Collection<MockTargetAmount> targetAmounts = getRandomTargetAmounts(user);

        String sql = String.format("""
                INSERT INTO `%s` (amount, is_read, user_id, created_at, updated_at)
                VALUES (:amount, true, :userId, :createdAt, :updatedAt)
                """, TARGET_AMOUNT_TABLE);
        SqlParameterSource[] params = targetAmounts.stream()
                .map(mockTargetAmount -> new MapSqlParameterSource()
                        .addValue("amount", mockTargetAmount.amount)
                        .addValue("userId", mockTargetAmount.userId)
                        .addValue("createdAt", mockTargetAmount.createdAt)
                        .addValue("updatedAt", mockTargetAmount.updatedAt))
                .toArray(SqlParameterSource[]::new);
        jdbcTemplate.batchUpdate(sql, params);
    }

    /**
     * 사용자의 임의의 년/월에 대한 목표 금액을 생성하는 메서드 (짝수 달에만 생성)
     */
    private static List<MockTargetAmount> getRandomTargetAmounts(User user) {
        List<MockTargetAmount> targetAmounts = new ArrayList<>();
        LocalDate startAt = user.getCreatedAt().toLocalDate(), endAt = LocalDate.now();
        int monthLength = (endAt.getYear() - startAt.getYear()) * 12 + (endAt.getMonthValue() - startAt.getMonthValue());

        for (int i = 0; i < monthLength + 1; i += 2) {
            targetAmounts.add(MockTargetAmount.of(
                    ThreadLocalRandom.current().nextInt(100, 10000001),
                    LocalDateTime.of(startAt.plusMonths(i).getYear(), startAt.plusMonths(i).getMonth(), 1, 0, 0, 0),
                    LocalDateTime.of(startAt.plusMonths(i).getYear(), startAt.plusMonths(i).getMonth(), 1, 0, 0, 0),
                    user.getId()
            ));
        }

        return targetAmounts;
    }

    public static void convertCreatedAt(TargetAmount targetAmount, LocalDateTime dateTime, NamedParameterJdbcTemplate jdbcTemplate, EntityManager em) {
        String sql = String.format("UPDATE `%s` SET created_at = :createdAt WHERE id = :id", TARGET_AMOUNT_TABLE);
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("createdAt", dateTime)
                .addValue("id", targetAmount.getId());
        jdbcTemplate.update(sql, param);
        em.clear();
    }

    public TargetAmount toTargetAmount(User user) {
        return TargetAmount.of(amount, user);
    }

    private record MockTargetAmount(int amount, LocalDateTime createdAt, LocalDateTime updatedAt, Long userId) {
        public static MockTargetAmount of(int amount, LocalDateTime createdAt, LocalDateTime updatedAt, Long userId) {
            return new MockTargetAmount(amount, createdAt, updatedAt, userId);
        }
    }
}
