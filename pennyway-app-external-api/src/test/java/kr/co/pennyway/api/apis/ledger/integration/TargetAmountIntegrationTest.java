package kr.co.pennyway.api.apis.ledger.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.SpendingFixture;
import kr.co.pennyway.api.config.fixture.TargetAmountFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.service.TargetAmountService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExternalApiIntegrationTest
@AutoConfigureMockMvc
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class TargetAmountIntegrationTest extends ExternalApiDBTestConfig {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private TargetAmountService targetAmountService;
    @PersistenceContext
    private EntityManager em;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private User createUserWithCreatedAt(LocalDateTime createdAt, NamedParameterJdbcTemplate jdbcTemplate) {
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
        Long userId = user.getId();

        UserFixture.updateUserCreatedAt(user, createdAt, jdbcTemplate);
        em.flush();
        em.clear();

        return userService.readUser(userId).orElseThrow();
    }

    @Nested
    @DisplayName("당월 목표 금액 등록/수정")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class PutTargetAmount {
        @Test
        @DisplayName("당월 목표 금액 entity가 존재하지 않을 경우 새로 생성한다.")
        @WithSecurityMockUser
        @Transactional
        void putTargetAmountNotFound() throws Exception {
            // given
            User user = UserFixture.GENERAL_USER.toUser();
            userService.createUser(user);
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // when
            ResultActions result = performPutTargetAmount(date, 100000, user);

            // then
            result.andExpect(status().isOk());
            assertNotNull(targetAmountService.readTargetAmountThatMonth(user.getId(), LocalDate.now()).orElse(null));
        }

        @Test
        @DisplayName("당월 목표 금액 entity가 존재하는 경우 amount를 수정한다.")
        @WithSecurityMockUser
        @Transactional
        void putTargetAmountFound() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            TargetAmount targetAmount = targetAmountService.createTargetAmount(TargetAmount.of(100000, user));

            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // when
            ResultActions result = performPutTargetAmount(date, 200000, user);

            // then
            result.andExpect(status().isOk());
            assertEquals(200000, targetAmount.getAmount());
        }

        private ResultActions performPutTargetAmount(String date, Integer amount, User requestUser) throws Exception {
            UserDetails userDetails = SecurityUserDetails.from(requestUser);
            return mockMvc.perform(put("/v2/target-amounts")
                    .with(user(userDetails))
                    .param("date", date)
                    .param("amount", amount.toString()));
        }
    }

    @Nested
    @DisplayName("임의의 년/월에 대한 사용자 목표 금액 및 지출 총합 조회")
    class GetTargetAmountAndTotalSpending {
        @Test
        @DisplayName("특정 년/월에 대한 사용자 목표 금액 및 지출 총합 조회")
        @Transactional
        void getTargetAmountAndTotalSpending() throws Exception {
            // given
            User user = createUserWithCreatedAt(LocalDateTime.now().minusYears(2), jdbcTemplate);
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

            return mockMvc.perform(MockMvcRequestBuilders.get("/v2/target-amounts/{date}", date)
                    .with(user(userDetails)));
        }
    }

    @Nested
    @DisplayName("사용자 목표 금액 및 지출 총합 전체 기록 조회")
    class GetTargetAmountsAndTotalSpendings {
        @Test
        @DisplayName("사용자 목표 금액 및 지출 총합 조회")
        @Transactional
        void getTargetAmountsAndTotalSpendings() throws Exception {
            // given
            User user = createUserWithCreatedAt(LocalDateTime.now().minusYears(2).plusMonths(2), jdbcTemplate);
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

            return mockMvc.perform(MockMvcRequestBuilders.get("/v2/target-amounts")
                    .with(user(userDetails))
                    .param("date", date.toString()));
        }
    }
}
