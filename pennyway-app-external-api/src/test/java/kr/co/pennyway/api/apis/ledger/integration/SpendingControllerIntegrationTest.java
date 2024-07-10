package kr.co.pennyway.api.apis.ledger.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.ledger.dto.SpendingIdsDto;
import kr.co.pennyway.api.apis.ledger.dto.SpendingMigrateDto;
import kr.co.pennyway.api.apis.ledger.dto.SpendingReq;
import kr.co.pennyway.api.common.query.SpendingCategoryType;
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
import java.util.ArrayList;
import java.util.List;

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
            SpendingReq request = new SpendingReq(10000, category.getId(), SpendingCategory.CUSTOM, LocalDate.now(), "소비처", "메모");

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
            SpendingReq request = new SpendingReq(10000, 1000L, SpendingCategory.CUSTOM, LocalDate.now(), "소비처", "메모");

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
            SpendingFixture.bulkInsertSpending(user, 150, 0L, jdbcTemplate);

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

    @Order(3)
    @Nested
    @DisplayName("지출 내역 상세 조회")
    class GetSpendingDetail {
        @Test
        @DisplayName("지출 내역 상세 조회 성공")
        @Transactional
        void getSpendingDetailSuccess() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            Spending spending = SpendingFixture.GENERAL_SPENDING.toSpending(user);
            spendingService.createSpending(spending);

            // when
            ResultActions resultActions = performGetSpendingDetailSuccess(user, spending.getId());

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.spending.id").value(spending.getId()));
        }

        @Test
        @DisplayName("사용자가 spendingId에 해당하는 지출내역의 작성자가 아닌 수정시 403 Forbidden을 반환한다.")
        @Transactional
        void getSpendingDetailForbidden() throws Exception {
            // given
            User user1 = userService.createUser(UserFixture.GENERAL_USER.toUser());

            Spending spending = SpendingFixture.GENERAL_SPENDING.toSpending(user1);
            spendingService.createSpending(spending);
            User user2 = userService.createUser(UserFixture.GENERAL_USER.toUser());

            // when
            ResultActions resultActions = performGetSpendingDetailSuccess(user2, spending.getId());

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        private ResultActions performGetSpendingDetailSuccess(User requestUser, Long spendingId) throws Exception {
            UserDetails userDetails = SecurityUserDetails.from(requestUser);

            return mockMvc.perform(MockMvcRequestBuilders.get("/v2/spendings/{spendingId}", spendingId)
                    .with(user(userDetails)));
        }
    }

    @Order(4)
    @Nested
    @DisplayName("지출 내역 수정")
    class UpdateSpending {
        @Test
        @DisplayName("지출 내역 수정 성공")
        void updateSpendingSuccess() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            Spending spending = spendingService.createSpending(SpendingFixture.GENERAL_SPENDING.toSpending(user));

            SpendingReq request = new SpendingReq(20000, -1L, SpendingCategory.LIVING, LocalDate.now(), "수정된 소비처", "수정된 메모");

            // when
            ResultActions resultActions = performUpdateSpending(request, user, spending.getId());

            // then
            resultActions.andDo(print()).andExpect(status().isOk());

            Spending updatedSpending = spendingService.readSpending(spending.getId()).get();
            Assertions.assertEquals(request.memo(), updatedSpending.getMemo());
        }

        @Test
        @DisplayName("사용자가 spendingId에 해당하는 지출내역의 작성자가 아닌 수정시 403 Forbidden을 반환한다.")
        @Transactional
        void updateSpendingForbidden() throws Exception {
            // given
            User user1 = userService.createUser(UserFixture.GENERAL_USER.toUser());
            Spending spending = SpendingFixture.GENERAL_SPENDING.toSpending(user1);
            spendingService.createSpending(spending);
            User user2 = userService.createUser(UserFixture.GENERAL_USER.toUser());
            SpendingReq request = SpendingFixture.toSpendingReq(user2);

            // when
            ResultActions resultActions = performUpdateSpending(request, user2, spending.getId());

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        private ResultActions performUpdateSpending(SpendingReq req, User requestUser, Long spendingId) throws Exception {
            UserDetails userDetails = SecurityUserDetails.from(requestUser);

            return mockMvc.perform(MockMvcRequestBuilders.put("/v2/spendings/{spendingId}", spendingId)
                    .contentType("application/json")
                    .with(user(userDetails))
                    .content(objectMapper.writeValueAsString(req)));
        }
    }

    @Order(5)
    @Nested
    @DisplayName("지출 내역 삭제")
    class DeleteSpending {

        @Test
        @DisplayName("지출 내역 삭제 성공")
        @Transactional
        void deleteSpendingSuccess() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            Spending spending = SpendingFixture.GENERAL_SPENDING.toSpending(user);
            spendingService.createSpending(spending);

            // when
            ResultActions resultActions = performDeleteSpendingSuccess(user, spending.getId());

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk());

            Assertions.assertTrue(spendingService.readSpending(spending.getId()).isEmpty());
        }

        @Test
        @DisplayName("사용자가 spendingId에 해당하는 지출 내역의 소유자가 아닌 경우, 403 Forbidden을 반환한다.")
        @Transactional
        void deleteSpendingForbidden() throws Exception {

            // given
            User user1 = userService.createUser(UserFixture.GENERAL_USER.toUser());
            Spending spending = SpendingFixture.GENERAL_SPENDING.toSpending(user1);
            spendingService.createSpending(spending);
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

    @Order(6)
    @Nested
    @DisplayName("지출 내역 복수 삭제")
    class DeleteSpendings {

        @Test
        @DisplayName("지출 내역 복수 삭제 성공")
        @Transactional
        void deleteSpendingsSuccess() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            List<Long> spendingIds = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                Spending spending = SpendingFixture.GENERAL_SPENDING.toSpending(user);
                spendingService.createSpending(spending);
                spendingIds.add(spending.getId());
            }
            SpendingIdsDto spendingIdsDto = new SpendingIdsDto(spendingIds);

            // when
            ResultActions resultActions = performDeleteSpendings(user, spendingIdsDto);

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk());

            for (Long id : spendingIds) {
                Assertions.assertTrue(spendingService.readSpending(id).isEmpty());
            }
        }

        @Test
        @DisplayName("spendingIds에 하나라도 소유하지 않은 지출 내역이 포함되어 있을 경우, 403 Forbidden을 반환한다.")
        @Transactional
        void deleteSpendingsForbidden() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
            User user2 = userService.createUser(UserFixture.GENERAL_USER.toUser());
            List<Long> spendingIds = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                Spending spending = SpendingFixture.GENERAL_SPENDING.toSpending(user);
                spendingService.createSpending(spending);
                spendingIds.add(spending.getId());
            }
            SpendingIdsDto spendingIdsDto = new SpendingIdsDto(spendingIds);

            // when
            ResultActions resultActions = performDeleteSpendings(user2, spendingIdsDto);

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        private ResultActions performDeleteSpendings(User requestUser, SpendingIdsDto req) throws Exception {
            UserDetails userDetails = SecurityUserDetails.from(requestUser);

            return mockMvc.perform(MockMvcRequestBuilders.delete("/v2/spendings", req)
                    .contentType("application/json")
                    .with(user(userDetails))
                    .content(objectMapper.writeValueAsString(req)));
        }
    }

    /* TODO : lazyLoading proxy 예외 문제 해결한 테스트코드 작성하기 */
    @Test
    @DisplayName("지출 내역 카테고리 이동 (커스텀 카테고리)")
    void migrateCategoryByCategoryId() throws Exception {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
        SpendingCustomCategory fromCategory = spendingCustomCategoryService.createSpendingCustomCategory(SpendingCustomCategory.of("잉여비", SpendingCategory.LIVING, user));
        SpendingCustomCategory toCategory = spendingCustomCategoryService.createSpendingCustomCategory(SpendingCustomCategory.of("식비", SpendingCategory.FOOD, user));

        Spending spending = spendingService.createSpending(SpendingFixture.CUSTOM_CATEGORY_SPENDING.toCustomCategorySpending(user, fromCategory));
        Long id = spending.getId();

        SpendingMigrateDto request = new SpendingMigrateDto(toCategory.getId(), SpendingCategoryType.CUSTOM);

        // when
        ResultActions resultActions = performMigrateCategory(user, fromCategory.getId(), request);

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk());

        // 변경된 엔티티를 다시 로딩
        Spending updatedSpending = spendingService.readSpending(id).orElseThrow(() -> new IllegalStateException("Spending not found"));

        Assertions.assertEquals(toCategory.getId(), updatedSpending.getSpendingCustomCategory().getId());
        log.info("조회한 지출내역의 커스텀 카테고리 id {}", updatedSpending.getSpendingCustomCategory().getName());
    }

    @Test
    @DisplayName("지출 내역 카테고리 이동 (기본 카테고리)")
    void migrateCategoryByCategory() throws Exception {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
        SpendingCustomCategory fromCategory = spendingCustomCategoryService.createSpendingCustomCategory(SpendingCustomCategory.of("잉여비", SpendingCategory.LIVING, user));
        Spending spending = spendingService.createSpending(SpendingFixture.GENERAL_SPENDING.toSpending(user));
        Long id = spending.getId();
        Long toCategoryId = Long.parseLong(SpendingCategory.LIVING.getCode());

        SpendingMigrateDto request = new SpendingMigrateDto(toCategoryId, SpendingCategoryType.DEFAULT);

        // when
        ResultActions resultActions = performMigrateCategory(user, fromCategory.getId(), request);

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk());

        // TODO : Spending.getCategory() 메서드가 category getter가 아니라서 이렇게 복잡하게 검증해야함... 개선 필요할까?
        Spending updatedSpending = spendingService.readSpending(id).get();
        Spending expectedSpending = SpendingFixture.GENERAL_SPENDING.toSpending(user);

        expectedSpending.update(
                updatedSpending.getAmount(),
                SpendingCategory.LIVING,
                updatedSpending.getSpendAt(),
                updatedSpending.getAccountName(),
                updatedSpending.getMemo(),
                null
        );

        // 아직 작업중...
        Assertions.assertNull(updatedSpending.getSpendingCustomCategory());
        log.info("{}", updatedSpending.getSpendingCustomCategory());
        Assertions.assertTrue(updatedSpending.getCategory().name().equals("생활"));
        log.info("{}", updatedSpending.getCategory().name());
    }


    private ResultActions performMigrateCategory(User requestUser, Long fromCategoryId, SpendingMigrateDto request) throws Exception {
        UserDetails userDetails = SecurityUserDetails.from(requestUser);

        return mockMvc.perform(MockMvcRequestBuilders.patch("/v2/spendings/migration/{fromCategoryId}", fromCategoryId)
                .with(user(userDetails))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));
    }
}