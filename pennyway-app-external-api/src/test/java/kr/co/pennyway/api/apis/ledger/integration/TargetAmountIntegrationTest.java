package kr.co.pennyway.api.apis.ledger.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.SpendingFixture;
import kr.co.pennyway.api.config.fixture.TargetAmountFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    @DisplayName("당월 목표 금액 등록")
    class PostTargetAmount {
        @Test
        @DisplayName("사용자에게 당월 목표 기록이 존재할 시 409 Conflict 에러 응답을 반환한다.")
        void postTargetAmountConflict() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            TargetAmount targetAmount = targetAmountService.createTargetAmount(TargetAmountFixture.GENERAL_TARGET_AMOUNT.toTargetAmount(user));
            log.debug("targetAmount: {}", targetAmount);

            // when
            ResultActions result = performPostTargetAmount(user, LocalDate.now());

            // then
            result.andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("사용자에게 당월 목표 기록이 존재하지 않을 시 200 OK 응답을 반환한다.")
        void postTargetAmountOk() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());

            // when
            ResultActions result = performPostTargetAmount(user, LocalDate.now());

            // then
            result.andDo(print())
                    .andExpect(status().isOk());
        }

        private ResultActions performPostTargetAmount(User requestUser, LocalDate date) throws Exception {
            UserDetails userDetails = SecurityUserDetails.from(requestUser);

            return mockMvc.perform(MockMvcRequestBuilders.post("/v2/target-amounts")
                    .with(user(userDetails))
                    .param("year", String.valueOf(date.getYear()))
                    .param("month", String.valueOf(date.getMonthValue())));
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

    @Nested
    @DisplayName("당월 목표 금액 등록/수정")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class PutTargetAmount {
        @Test
        @DisplayName("당월 목표 금액 pk에 대한 접근 권한이 없는 경우 403 Forbidden 에러 응답을 반환한다.")
        @Transactional
        void putTargetAmountForbidden() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());

            // when
            ResultActions result = performPutTargetAmount(1000L, 100000, user);

            // then
            result.andDo(print()).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("당월 목표 금액 pk에 대한 접근 권한이 있지만, 당월 데이터가 아닌 경우 400 Bad Request 에러 응답을 반환한다.")
        @Transactional
        void putTargetAmountNotThatMonth() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            TargetAmount targetAmount = targetAmountService.createTargetAmount(TargetAmount.of(100000, user));
            TargetAmountFixture.convertCreatedAt(targetAmount, LocalDateTime.now().minusMonths(1), jdbcTemplate, em);
            targetAmount = targetAmountService.readTargetAmount(targetAmount.getId()).orElseThrow();

            // when
            ResultActions result = performPutTargetAmount(targetAmount.getId(), 200000, user);

            // then
            result.andDo(print()).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("당월 목표 금액 pk에 대한 접근 권한이 있고, 당월 데이터인 경우 200 OK 응답을 반환한다.")
        @Transactional
        void putTargetAmountCorrect() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            TargetAmount targetAmount = targetAmountService.createTargetAmount(TargetAmount.of(100000, user));

            // when
            ResultActions result = performPutTargetAmount(targetAmount.getId(), 200000, user);

            // then
            result.andDo(print()).andExpect(status().isOk());
            assertEquals(200000, targetAmountService.readTargetAmount(targetAmount.getId()).orElseThrow().getAmount());
        }

        private ResultActions performPutTargetAmount(Long targetAmountId, Integer amount, User requestUser) throws Exception {
            UserDetails userDetails = SecurityUserDetails.from(requestUser);
            return mockMvc.perform(patch("/v2/target-amounts/{target_amount_id}", targetAmountId)
                    .with(user(userDetails))
                    .param("amount", amount.toString()));
        }
    }

    @Nested
    @DisplayName("당월 목표 금액 삭제")
    class DeleteTargetAmount {
        @Test
        @DisplayName("당월 목표 금액 pk에 대한 접근 권한이 없는 경우 403 Forbidden 에러 응답을 반환한다.")
        @Transactional
        void deleteTargetAmountForbidden() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());

            // when
            ResultActions result = performDeleteTargetAmount(1000L, user);

            // then
            result.andDo(print()).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("당월 목표 금액 pk에 대한 접근 권한이 있지만, 당월 데이터가 아닌 경우 400 Bad Request 에러 응답을 반환한다.")
        @Transactional
        void deleteTargetAmountNotThatMonth() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            TargetAmount targetAmount = targetAmountService.createTargetAmount(TargetAmount.of(100000, user));
            TargetAmountFixture.convertCreatedAt(targetAmount, LocalDateTime.now().minusMonths(1), jdbcTemplate, em);
            targetAmount = targetAmountService.readTargetAmount(targetAmount.getId()).orElseThrow();

            // when
            ResultActions result = performDeleteTargetAmount(targetAmount.getId(), user);

            // then
            result.andDo(print()).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("당월 목표 금액 pk에 대한 접근 권한이 있지만, amount가 이미 -1인 경우 404 Not Found 에러 응답을 반환한다.")
        @Transactional
        void deleteTargetAmountNotFound() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            TargetAmount targetAmount = targetAmountService.createTargetAmount(TargetAmount.of(-1, user));

            // when
            ResultActions result = performDeleteTargetAmount(targetAmount.getId(), user);

            // then
            result.andDo(print()).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("당월 목표 금액 pk에 대한 접근 권한이 있고, 당월 데이터인 경우 200 OK 응답을 반환한다.")
        @Transactional
        void deleteTargetAmountCorrect() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            TargetAmount targetAmount = targetAmountService.createTargetAmount(TargetAmount.of(100000, user));

            // when
            ResultActions result = performDeleteTargetAmount(targetAmount.getId(), user);

            // then
            result.andDo(print()).andExpect(status().isOk());
        }

        private ResultActions performDeleteTargetAmount(Long targetAmountId, User requestUser) throws Exception {
            UserDetails userDetails = SecurityUserDetails.from(requestUser);
            return mockMvc.perform(MockMvcRequestBuilders.delete("/v2/target-amounts/{target_amount_id}", targetAmountId)
                    .with(user(userDetails)));
        }
    }
}
