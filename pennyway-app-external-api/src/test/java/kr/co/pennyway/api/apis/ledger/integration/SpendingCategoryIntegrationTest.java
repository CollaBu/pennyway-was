package kr.co.pennyway.api.apis.ledger.integration;

import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
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
    private SpendingCustomCategoryService spendingCustomCategoryService;

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


}
