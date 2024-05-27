package kr.co.pennyway.api.apis.ledger.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.ledger.dto.SpendingReq;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.SpendingFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.service.SpendingCustomCategoryService;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExternalApiIntegrationTest
@AutoConfigureMockMvc
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class SpendingControllerIntegrationTest extends ExternalApiDBTestConfig {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private SpendingService spendingService;
    @Autowired
    private SpendingCustomCategoryService spendingCustomCategoryService;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;


    @Order(1)
    @Nested
    @DisplayName("지출 내역 추가하기")
    class CreateSpending {
        @Test
        @DisplayName("request의 categoryId가 -1인 경우, spendingCustomCategory가 null인 Spending을 생성한다.")
        @Transactional
        void createSpendingSuccess() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            SpendingReq request = new SpendingReq(10000, -1L, SpendingCategory.FOOD, LocalDate.now(), "소비처", "메모");

            // when
            ResultActions result = performCreateSpendingSuccess(request, user);

            // then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.spending.amount").value(10000))
                    .andExpect(jsonPath("$.data.spending.category.isCustom").value(false))
                    .andExpect(jsonPath("$.data.spending.category.id").value(-1))
                    .andExpect(jsonPath("$.data.spending.category.icon").value(SpendingCategory.FOOD.name()));
        }

        @Test
        @DisplayName("request의 categoryId가 -1이 아닌 경우, spendingCustomCategory를 참조하는 Spending을 생성한다.")
        @Transactional
        void createSpendingWithCustomCategorySuccess() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            SpendingCustomCategory category = spendingCustomCategoryService.createSpendingCustomCategory(SpendingCustomCategory.of("잉여비", SpendingCategory.LIVING, user));
            SpendingReq request = new SpendingReq(10000, category.getId(), SpendingCategory.OTHER, LocalDate.now(), "소비처", "메모");

            // when
            ResultActions result = performCreateSpendingSuccess(request, user);

            // then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.spending.amount").value(10000))
                    .andExpect(jsonPath("$.data.spending.category.isCustom").value(true))
                    .andExpect(jsonPath("$.data.spending.category.id").value(category.getId()))
                    .andExpect(jsonPath("$.data.spending.category.icon").value(category.getIcon().name()));
        }

        @Test
        @DisplayName("사용자가 categoryId에 해당하는 카테고리 정보의 소유자가 아닌 경우, 403 Forbidden을 반환한다.")
        @Transactional
        void createSpendingWithInvalidCustomCategory() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            SpendingReq request = new SpendingReq(10000, 1000L, SpendingCategory.OTHER, LocalDate.now(), "소비처", "메모");

            // when
            ResultActions result = performCreateSpendingSuccess(request, user);

            // then
            result.andDo(print()).andExpect(status().isForbidden());
        }

        private ResultActions performCreateSpendingSuccess(SpendingReq req, User requestUser) throws Exception {
            UserDetails userDetails = SecurityUserDetails.from(requestUser);

            return mockMvc.perform(MockMvcRequestBuilders
                    .post("/v2/spendings")
                    .contentType("application/json")
                    .with(user(userDetails))
                    .content(objectMapper.writeValueAsString(req)));
        }
    }

    @Order(2)
    @Nested
    @DisplayName("월별 지출 내역 조회")
    class GetSpendingListAtYearAndMonth {
        @Test
        @DisplayName("월별 지출 내역 조회")
        @Transactional
        void getSpendingListAtYearAndMonthSuccess() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            SpendingFixture.bulkInsertSpending(user, 150, jdbcTemplate);

            // when
            long before = System.currentTimeMillis();
            ResultActions resultActions = performGetSpendingListAtYearAndMonthSuccess(user);
            long after = System.currentTimeMillis();

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk());
            log.debug("수행 시간: {}ms", after - before);
        }


        private ResultActions performGetSpendingListAtYearAndMonthSuccess(User requestUser) throws Exception {
            UserDetails userDetails = SecurityUserDetails.from(requestUser);
            LocalDate now = LocalDate.now();

            return mockMvc.perform(MockMvcRequestBuilders.get("/v2/spendings")
                    .with(user(userDetails))
                    .param("year", String.valueOf(now.getYear()))
                    .param("month", String.valueOf(now.getMonthValue())));
        }
    }

    @Order(4)
    @Nested
    @DisplayName("지출 내역 삭제")
    class DeleteSpending {

        @Test
        @DisplayName("지출 내역 삭제 성공")
        @Transactional
        void deleteSpendingSuccess() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            Spending spending = spendingService.createSpending(Spending.builder()
                    .amount(10000)
                    .category(SpendingCategory.FOOD)
                    .spendAt(LocalDateTime.now())
                    .accountName("소비처")
                    .memo("메모")
                    .user(user)
                    .spendingCustomCategory(null)
                    .build());

            // when
            ResultActions resultActions = performDeleteSpendingSuccess(user, spending.getId());

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("권한이 없는 사용자의 지출 내역 삭제")
        @Transactional
        void deleteSpendingForbidden() throws Exception {
            // given
            User user1 = userService.createUser(UserFixture.GENERAL_USER.toUser());
            Spending spending = spendingService.createSpending(Spending.builder()
                    .amount(10000)
                    .category(SpendingCategory.FOOD)
                    .spendAt(LocalDateTime.now())
                    .accountName("소비처")
                    .memo("메모")
                    .user(user1)
                    .spendingCustomCategory(null)
                    .build());
            User user2 = userService.createUser(UserFixture.GENERAL_USER.toUser());

            // when
            ResultActions resultActions = performDeleteSpendingSuccess(user2, spending.getId());

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        private ResultActions performDeleteSpendingSuccess(User requestUser, Long spendingId) throws Exception {
            UserDetails userDetails = SecurityUserDetails.from(requestUser);

            return mockMvc.perform(MockMvcRequestBuilders.delete("/v2/spendings/{spendingId}", spendingId)
                    .with(user(userDetails)));
        }
    }

}
