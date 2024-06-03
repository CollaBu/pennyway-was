package kr.co.pennyway.api.config.fixture;

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

public class TargetAmountFixture {
    private static final String TARGET_AMOUNT_TABLE = "target_amount";

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

    private record MockTargetAmount(int amount, LocalDateTime createdAt, LocalDateTime updatedAt, Long userId) {
        public static MockTargetAmount of(int amount, LocalDateTime createdAt, LocalDateTime updatedAt, Long userId) {
            return new MockTargetAmount(amount, createdAt, updatedAt, userId);
        }
    }
}
