package kr.co.pennyway.api.apis.ledger.integration;

import kr.co.pennyway.api.common.query.SpendingCategoryType;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
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
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExternalApiIntegrationTest
@AutoConfigureMockMvc
public class SpendingCategoryIntegrationTest extends ExternalApiDBTestConfig {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SpendingService spendingService;
    @Autowired
    private UserService userService;
    @Autowired
    private SpendingCategoryService spendingCustomCategoryService;

    @Test
    @DisplayName("사용자 정의 지출 카테고리를 삭제하고, 삭제된 카테고리를 가지는 지출 내역 또한 삭제된다.")
    @Transactional
    void deleteSpendingCustomCategory() throws Exception {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
        SpendingCustomCategory spendingCustomCategory = spendingCustomCategoryService.createSpendingCustomCategory(SpendingCustomCategoryFixture.GENERAL_SPENDING_CUSTOM_CATEGORY.toCustomSpendingCategory(user));
        Spending spending = spendingService.createSpending(SpendingFixture.CUSTOM_CATEGORY_SPENDING.toCustomCategorySpending(user, spendingCustomCategory));

        // when
        ResultActions resultActions = performDeleteSpendingCategory(spendingCustomCategory.getId(), user);

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk());

        Assertions.assertTrue(spendingCustomCategoryService.readSpendingCustomCategory(spendingCustomCategory.getId()).isEmpty());
        Assertions.assertTrue(spendingService.readSpending(spending.getId()).isEmpty());
    }

    private ResultActions performDeleteSpendingCategory(Long categoryId, User requestUser) throws Exception {
        UserDetails userDetails = SecurityUserDetails.from(requestUser);

        return mockMvc.perform(MockMvcRequestBuilders.delete("/v2/spending-categories/{categoryId}", categoryId)
                .with(user(userDetails)));
    }


    @Test
    @DisplayName("사용자는 커스텀 카테고리에서 기본 카테고리로 지출내역들을 옮길 수 있다.")
    void migrateSpendingsCtoD() throws Exception {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
        SpendingCustomCategory fromCategory = spendingCustomCategoryService.createSpendingCustomCategory(SpendingCustomCategoryFixture.GENERAL_SPENDING_CUSTOM_CATEGORY.toCustomSpendingCategory(user));
        Spending spending = spendingService.createSpending(SpendingFixture.CUSTOM_CATEGORY_SPENDING.toCustomCategorySpending(user, fromCategory));
        Long spendingId = spending.getId();
        SpendingCategory toCategory = SpendingCategory.TRANSPORTATION;

        // when
        ResultActions resultActions = performMigrateSpendingsByCategory(fromCategory.getId(), SpendingCategoryType.CUSTOM, 2L, SpendingCategoryType.DEFAULT, user);

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk());

        Spending spendingAfterMigration = spendingService.readSpending(spendingId).orElseThrow();
        Assertions.assertEquals(spendingAfterMigration.getCategory().icon(), toCategory);
        Assertions.assertEquals(spendingAfterMigration.getSpendingCustomCategory(), null);
    }

    @Test
    @DisplayName("사용자는 커스텀 카테고리에서 커스텀 카테고리로 지출내역들을 옮길 수 있다.")
    void migrateSpendingsCtoC() throws Exception {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
        SpendingCustomCategory fromCategory = spendingCustomCategoryService.createSpendingCustomCategory(SpendingCustomCategoryFixture.GENERAL_SPENDING_CUSTOM_CATEGORY.toCustomSpendingCategory(user));
        Spending spending = spendingService.createSpending(SpendingFixture.CUSTOM_CATEGORY_SPENDING.toCustomCategorySpending(user, fromCategory));

        SpendingCustomCategory toCategory = spendingCustomCategoryService.createSpendingCustomCategory(SpendingCustomCategoryFixture.GENERAL_SPENDING_CUSTOM_CATEGORY.toCustomSpendingCategory(user));
        Long toCategoryId = toCategory.getId();
        Long spendingId = spending.getId();

        // when
        ResultActions resultActions = performMigrateSpendingsByCategory(fromCategory.getId(), SpendingCategoryType.CUSTOM, toCategoryId, SpendingCategoryType.CUSTOM, user);

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk());

        Spending spendingAfterMigration = spendingService.readSpending(spendingId).orElseThrow();
        Assertions.assertEquals(spendingAfterMigration.getSpendingCustomCategory().getId(), toCategory.getId());
    }

    @Test
    @DisplayName("사용자는 기본 카테고리에서 커스텀 카테고리로 지출내역들을 옮길 수 있다.")
    void migrateSpendingsDtoC() throws Exception {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
        Spending spending = spendingService.createSpending(SpendingFixture.GENERAL_SPENDING.toSpending(user));

        SpendingCustomCategory toCategory = spendingCustomCategoryService.createSpendingCustomCategory(SpendingCustomCategoryFixture.GENERAL_SPENDING_CUSTOM_CATEGORY.toCustomSpendingCategory(user));
        Long toCategoryId = toCategory.getId();
        Long spendingId = spending.getId();

        // when
        ResultActions resultActions = performMigrateSpendingsByCategory(1L, SpendingCategoryType.DEFAULT, toCategoryId, SpendingCategoryType.CUSTOM, user);

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk());

        Spending spendingAfterMigration = spendingService.readSpending(spendingId).orElseThrow();
        Assertions.assertEquals(spendingAfterMigration.getSpendingCustomCategory().getId(), toCategory.getId());
    }

    @Test
    @DisplayName("사용자는 기본 카테고리에서 기본 카테고리로 지출내역들을 옮길 수 있다.")
    void migrateSpendingsDtoD() throws Exception {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
        Spending spending = spendingService.createSpending(SpendingFixture.GENERAL_SPENDING.toSpending(user));
        Long spendingId = spending.getId();
        SpendingCategory toCategory = SpendingCategory.TRANSPORTATION;

        // when
        ResultActions resultActions = performMigrateSpendingsByCategory(1L, SpendingCategoryType.DEFAULT, 2L, SpendingCategoryType.DEFAULT, user);

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk());

        Spending spendingAfterMigration = spendingService.readSpending(spendingId).orElseThrow();
        Assertions.assertEquals(spendingAfterMigration.getCategory().icon(), toCategory);
    }

    private ResultActions performMigrateSpendingsByCategory(Long fromId, SpendingCategoryType fromType, Long toId, SpendingCategoryType toType, User requestUser) throws Exception {
        UserDetails userDetails = SecurityUserDetails.from(requestUser);

        return mockMvc.perform(MockMvcRequestBuilders.patch("/v2/spending-categories/{fromId}/migration", fromId)
                .param("fromType", fromType.toString())
                .param("toId", toId.toString())
                .param("toType", toType.toString())
                .with(user(userDetails))
                .contentType("application/json"));
    }

}
