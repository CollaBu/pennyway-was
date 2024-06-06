package kr.co.pennyway.api.apis.ledger.service;

import jakarta.persistence.EntityManager;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.SpendingCustomCategoryFixture;
import kr.co.pennyway.api.config.fixture.SpendingFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.service.SpendingCustomCategoryService;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@ExtendWith(MockitoExtension.class)
@ExternalApiIntegrationTest
class SpendingSearchServiceTest extends ExternalApiDBTestConfig {
    @Autowired
    private SpendingService spendingService;
    @Autowired
    private EntityManager em;
    @Autowired
    private SpendingCustomCategoryService spendingCustomCategoryService;
    @Autowired
    private UserService userService;

    @Test
    @Transactional
    void testReadSpendingsLazyLoading() {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());

        SpendingCustomCategory customCategory = SpendingCustomCategoryFixture.GENERAL_SPENDING_CUSTOM_CATEGORY.toCustomSpendingCategory(user);
        spendingCustomCategoryService.createSpendingCustomCategory(customCategory);

        Spending spending = SpendingFixture.CUSTOM_CATEGORY_SPENDING.toCustomCategorySpending(user, customCategory);
        spendingService.createSpending(spending);

        // when - then
        Assertions.assertThrows(LazyInitializationException.class, () -> {
            em.clear();
            spending.getSpendingCustomCategory().getName();
        });
    }
}