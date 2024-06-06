package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.SpendingFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Slf4j
@ExtendWith(MockitoExtension.class)
@ExternalApiIntegrationTest
class SpendingSearchServiceTest extends ExternalApiDBTestConfig {
    @Autowired
    private SpendingSearchService spendingSearchService;
    @Autowired
    private UserService userService;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    @DisplayName("커스텀 카테고리 지출 내역을 기간별 조회시 카테고리를 바로 fetch 한다.")
    void testReadSpendingsLazyLoading() {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
        SpendingFixture.bulkInsertSpending(user, 100, true, jdbcTemplate);

        // when - then
        List<Spending> spendings = spendingSearchService.readSpendings(user.getId(), LocalDate.now().getYear(), LocalDate.now().getMonthValue());
        assertDoesNotThrow(() -> {
            String categoryName = spendings.get(0).getSpendingCustomCategory().getName();
            log.info("{}", categoryName);
        });
    }
}
