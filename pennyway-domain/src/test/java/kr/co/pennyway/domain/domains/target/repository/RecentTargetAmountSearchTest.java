package kr.co.pennyway.domain.domains.target.repository;

import kr.co.pennyway.domain.config.ContainerMySqlTestConfig;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.config.TestJpaConfig;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create"})
@ContextConfiguration(classes = JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
public class RecentTargetAmountSearchTest extends ContainerMySqlTestConfig {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TargetAmountRepository targetAmountRepository;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("사용자의 가장 최근 목표 금액을 조회할 수 있다.")
    @Transactional
    public void 가장_최근_사용자_목표_금액_조회() {
        // given
        User user = userRepository.save(User.builder().username("jayang").name("Yang").phone("010-0000-0000").build());
        bulkInsertTargetAmount(user);

        // when - then
        targetAmountRepository.findRecentOneByUserId(user.getId())
                .ifPresentOrElse(
                        targetAmount -> assertEquals(targetAmount.getAmount(), 30000),
                        () -> Assertions.fail("최근 목표 금액이 존재하지 않습니다.")
                );
    }

    private void bulkInsertTargetAmount(User user) {
        Collection<MockTargetAmount> targetAmounts = getMockTargetAmounts();

        String sql = String.format("""
                INSERT INTO `%s` (amount, is_read, user_id, created_at, updated_at)
                VALUES (:amount, true, :userId, :createdAt, :updatedAt)
                """, "target_amount");
        SqlParameterSource[] params = targetAmounts.stream()
                .map(mockTargetAmount -> new MapSqlParameterSource()
                        .addValue("amount", mockTargetAmount.amount)
                        .addValue("userId", user.getId())
                        .addValue("createdAt", mockTargetAmount.createdAt)
                        .addValue("updatedAt", mockTargetAmount.updatedAt))
                .toArray(SqlParameterSource[]::new);
        jdbcTemplate.batchUpdate(sql, params);
    }

    private Collection<MockTargetAmount> getMockTargetAmounts() {
        return List.of(
                MockTargetAmount.of(10000, true, LocalDateTime.of(2021, 1, 1, 0, 0, 0), LocalDateTime.of(2021, 1, 1, 0, 0, 0)),
                MockTargetAmount.of(-1, false, LocalDateTime.of(2022, 3, 1, 0, 0, 0), LocalDateTime.of(2022, 3, 1, 0, 0, 0)),
                MockTargetAmount.of(20000, true, LocalDateTime.of(2022, 5, 1, 0, 0, 0), LocalDateTime.of(2022, 5, 1, 0, 0, 0)),
                MockTargetAmount.of(30000, true, LocalDateTime.of(2023, 7, 1, 0, 0, 0), LocalDateTime.of(2023, 7, 1, 0, 0, 0)),
                MockTargetAmount.of(-1, false, LocalDateTime.of(2024, 1, 1, 0, 0, 0), LocalDateTime.of(2024, 1, 1, 0, 0, 0)),
                MockTargetAmount.of(-1, true, LocalDateTime.of(2024, 2, 1, 0, 0, 0), LocalDateTime.of(2024, 2, 1, 0, 0, 0))
        );
    }

    private record MockTargetAmount(int amount, boolean isRead, LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static MockTargetAmount of(int amount, boolean isRead, LocalDateTime createdAt, LocalDateTime updatedAt) {
            return new MockTargetAmount(amount, isRead, createdAt, updatedAt);
        }
    }
}
