package kr.co.pennyway.api.apis.ledger.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.SpendingCustomCategoryFixture;
import kr.co.pennyway.api.config.fixture.SpendingFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.context.account.service.UserService;
import kr.co.pennyway.domain.context.finance.service.SpendingCategoryService;
import kr.co.pennyway.domain.context.finance.service.SpendingService;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@ExtendWith(MockitoExtension.class)
@ExternalApiIntegrationTest
class SpendingSearchServiceTest extends ExternalApiDBTestConfig {
    @Autowired
    private UserService userService;
    @Autowired
    private SpendingService spendingService;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private SpendingCategoryService spendingCategoryService;

    @PersistenceContext
    private EntityManager entityManager;
    private Statistics statistics;


    @BeforeEach
    public void setUp() {
        SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) ((SessionImplementor) entityManager.getDelegate()).getSessionFactory();
        statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        statistics.clear();
    }

    @Test
    @Transactional
    @DisplayName("커스텀 카테고리 지출 내역을 기간별 조회시 카테고리를 바로 fetch 한다.")
    void testReadSpendingsLazyLoading() {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
        SpendingCustomCategory spendingCustomCategory = SpendingCustomCategoryFixture.GENERAL_SPENDING_CUSTOM_CATEGORY.toCustomSpendingCategory(user);
        spendingCategoryService.createSpendingCustomCategory(spendingCustomCategory);
        SpendingFixture.bulkInsertSpending(user, 100, spendingCustomCategory.getId(), jdbcTemplate);

        // when
        List<Spending> spendings = spendingService.readSpendings(user.getId(), LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        int size = spendings.size();
        for (Spending spending : spendings) {
            log.info("지출내역 id : {} 커스텀 카테고리 id : {} 커스텀 카테고리 name : {}",
                    spending.getId(),
                    spending.getSpendingCustomCategory().getId(),
                    spending.getSpendingCustomCategory().getName()
            );
        }

        // then
        log.info("쿼리문 실행 횟수: {}", statistics.getPrepareStatementCount());
        log.info("readSpendings로 조회해온 지출 내역 개수: {}", size);
        Assertions.assertEquals(3, statistics.getPrepareStatementCount());

        boolean isSortedDescending = IntStream.range(0, spendings.size() - 1)
                .allMatch(i -> !spendings.get(i).getSpendAt().isBefore(spendings.get(i + 1).getSpendAt()));
        Assertions.assertTrue(isSortedDescending);
    }
}
