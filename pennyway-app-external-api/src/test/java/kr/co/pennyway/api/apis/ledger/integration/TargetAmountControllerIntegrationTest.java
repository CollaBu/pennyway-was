package kr.co.pennyway.api.apis.ledger.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.SpendingFixture;
import kr.co.pennyway.api.config.fixture.TargetAmountFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExternalApiIntegrationTest
@AutoConfigureMockMvc
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class TargetAmountControllerIntegrationTest extends ExternalApiDBTestConfig {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private UserService userService;

    @Order(1)
    @Nested
    @DisplayName("임의의 년/월에 대한 사용자 목표 금액 및 지출 총합 조회")
    class GetTargetAmountAndTotalSpending {
        @Test
        @DisplayName("특정 년/월에 대한 사용자 목표 금액 및 지출 총합 조회")
        @Transactional
        void getTargetAmountAndTotalSpending() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            UserFixture.updateUserCreatedAt(user, user.getCreatedAt().minusMonths(10), jdbcTemplate);
            SpendingFixture.bulkInsertSpending(user, 300, jdbcTemplate);
            TargetAmountFixture.bulkInsertTargetAmount(user, jdbcTemplate);

            // when
            ResultActions result = performGetTargetAmountAndTotalSpending(user, LocalDate.now());

            // then
            result.andDo(print())
                    .andExpect(status().isOk());
        }

        private ResultActions performGetTargetAmountAndTotalSpending(User requestUser, LocalDate date) throws Exception {
            UserDetails userDetails = SecurityUserDetails.from(requestUser);

            return mockMvc.perform(MockMvcRequestBuilders.get("/v2/targets/{date}", date)
                    .with(user(userDetails)));
        }
    }

    @Order(2)
    @Nested
    @DisplayName("사용자 목표 금액 및 지출 총합 조회")
    class GetTargetAmountsAndTotalSpendings {
        @Test
        @DisplayName("사용자 목표 금액 및 지출 총합 조회")
        @Transactional
        void getTargetAmountsAndTotalSpendings() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            UserFixture.updateUserCreatedAt(user, user.getCreatedAt().minusMonths(10), jdbcTemplate);
            SpendingFixture.bulkInsertSpending(user, 300, jdbcTemplate);
            TargetAmountFixture.bulkInsertTargetAmount(user, jdbcTemplate);

            // when
            ResultActions result = performGetTargetAmountsAndTotalSpendings(user, LocalDate.now());

            // then
            result.andDo(print())
                    .andExpect(status().isOk());
        }

        private ResultActions performGetTargetAmountsAndTotalSpendings(User requestUser, LocalDate date) throws Exception {
            UserDetails userDetails = SecurityUserDetails.from(requestUser);

            return mockMvc.perform(MockMvcRequestBuilders.get("/v2/targets")
                    .with(user(userDetails))
                    .param("date", date.toString()));
        }
    }
}
