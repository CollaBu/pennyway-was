package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.repository.SpendingCustomCategoryRepository;
import kr.co.pennyway.domain.domains.spending.repository.SpendingRepository;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ExternalApiIntegrationTest
public class DailySpendingAggregateServiceTest extends ExternalApiDBTestConfig {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpendingRepository spendingRepository;

    @Autowired
    private SpendingCustomCategoryRepository spendingCustomCategoryRepository;

    @Autowired
    private DailySpendingAggregateService dailySpendingAggregateService;

    private static Spending createSpending(String accountName, LocalDateTime spendAt, SpendingCategory category, Integer amount, SpendingCustomCategory spendingCustomCategory, User user) {
        return Spending.builder()
                .accountName(accountName)
                .spendAt(spendAt)
                .category(category)
                .amount(amount)
                .spendingCustomCategory(spendingCustomCategory)
                .user(user)
                .build();
    }

    @Test
    public void test() {
        // given
        var user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        var spendingCustomCategory1 = spendingCustomCategoryRepository.save(SpendingCustomCategory.of("커스텀1", SpendingCategory.EDUCATION, user));
        var spendingCustomCategory2 = spendingCustomCategoryRepository.save(SpendingCustomCategory.of("커스텀2", SpendingCategory.FOOD, user));

        var today = LocalDateTime.now();

        var defaultFoodSpending1 = spendingRepository.save(createSpending("시스템 카테고리 지출1", today, SpendingCategory.FOOD, 10000, null, user));
        var defaultFoodSpending2 = spendingRepository.save(createSpending("시스템 카테고리 지출2", today, SpendingCategory.FOOD, 20000, null, user));
        var defaultEducationSpending1 = spendingRepository.save(createSpending("시스템 카테고리 지출3", today, SpendingCategory.EDUCATION, 30000, null, user));
        var defaultEducationSpending2 = spendingRepository.save(createSpending("시스템 카테고리 지출4", today, SpendingCategory.EDUCATION, 40000, null, user));
        var systemEducationSpending1 = spendingRepository.save(createSpending("커스텀 카테고리 지출1", today, SpendingCategory.CUSTOM, 50000, spendingCustomCategory1, user));
        var systemEducationSpending2 = spendingRepository.save(createSpending("커스텀 카테고리 지출2", today, SpendingCategory.CUSTOM, 60000, spendingCustomCategory1, user));
        var systemFoodSpending1 = spendingRepository.save(createSpending("커스텀 카테고리 지출3", today, SpendingCategory.CUSTOM, 70000, spendingCustomCategory2, user));
        var systemFoodSpending2 = spendingRepository.save(createSpending("커스텀 카테고리 지출4", today, SpendingCategory.CUSTOM, 80000, spendingCustomCategory2, user));

        // when
        var result = dailySpendingAggregateService.execute(user.getId(), today.getYear(), today.getMonthValue(), today.getDayOfMonth());

        // then
        assertEquals(result.get(0).getFirst(), systemFoodSpending1.getCategory());
        assertEquals(result.get(0).getSecond(), systemFoodSpending1.getAmount() + systemFoodSpending2.getAmount());
        assertEquals(result.get(1).getFirst(), systemEducationSpending1.getCategory());
        assertEquals(result.get(1).getSecond(), systemEducationSpending1.getAmount() + systemEducationSpending2.getAmount());
        assertEquals(result.get(2).getFirst(), defaultEducationSpending1.getCategory());
        assertEquals(result.get(2).getSecond(), defaultEducationSpending1.getAmount() + defaultEducationSpending2.getAmount());
        assertEquals(result.get(3).getFirst(), defaultFoodSpending1.getCategory());
        assertEquals(result.get(3).getSecond(), defaultFoodSpending1.getAmount() + defaultFoodSpending2.getAmount());
    }
}
