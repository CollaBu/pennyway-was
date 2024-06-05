package kr.co.pennyway.api.apis.ledger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.ledger.dto.SpendingReq;
import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.api.apis.ledger.usecase.SpendingUseCase;
import kr.co.pennyway.api.config.WebConfig;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = SpendingController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)})
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class SpendingControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SpendingUseCase spendingUseCase;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(post("/**").with(csrf()))
                .build();
    }

    @Order(1)
    @Nested
    @DisplayName("지출 내역 추가하기")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class postSpending {
        @Test
        @DisplayName("금액이 0이하의 정수인 경우 422 Unprocessable Entity를 반환한다.")
        @WithSecurityMockUser
        void whenAmountIsZeroOrNegative() throws Exception {
            // given
            int amount = 0;
            SpendingReq request = new SpendingReq(amount, -1L, SpendingCategory.FOOD, LocalDate.now(), "소비처", "메모");
            given(spendingUseCase.createSpending(1L, request)).willReturn(SpendingSearchRes.Individual.builder().build());

            // when
            ResultActions result = performPostSpending(request);

            // then
            result.andDo(print()).andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("아이콘이 CUSTOM이면서 categoryId가 -1인 경우 400 Bad Request를 반환한다.")
        @WithSecurityMockUser
        void whenCategoryIsNotDefined() throws Exception {
            // given
            Long categoryId = -1L;
            SpendingCategory icon = SpendingCategory.CUSTOM;
            SpendingReq request = new SpendingReq(10000, categoryId, icon, LocalDate.now(), "소비처", "메모");
            given(spendingUseCase.createSpending(1L, request)).willReturn(SpendingSearchRes.Individual.builder().build());

            // when
            ResultActions result = performPostSpending(request);

            // then
            result.andDo(print()).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("아이콘이 OTHER이면서 categoryId가 -1인 경우 400 Bad Request를 반환한다.")
        @WithSecurityMockUser
        void whenCategoryIsInvalidIcon() throws Exception {
            // given
            Long categoryId = -1L;
            SpendingCategory icon = SpendingCategory.OTHER;
            SpendingReq request = new SpendingReq(10000, categoryId, icon, LocalDate.now(), "소비처", "메모");
            given(spendingUseCase.createSpending(1L, request)).willReturn(SpendingSearchRes.Individual.builder().build());

            // when
            ResultActions result = performPostSpending(request);

            // then
            result.andDo(print()).andExpect(status().isBadRequest());
        }


        @Test
        @DisplayName("지출일이 현재보다 미래인 경우 422 Unprocessable Entity를 반환한다.")
        @WithSecurityMockUser
        void whenSpendAtIsFuture() throws Exception {
            // given
            LocalDate spendAt = LocalDate.now().plusDays(1);
            SpendingReq request = new SpendingReq(10000, -1L, SpendingCategory.FOOD, spendAt, "소비처", "메모");
            given(spendingUseCase.createSpending(1L, request)).willReturn(SpendingSearchRes.Individual.builder().build());

            // when
            ResultActions result = performPostSpending(request);

            // then
            result.andDo(print()).andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("소비처가 null이 아니면서 20자를 초과하는 경우 422 Unprocessable Entity를 반환한다.")
        @WithSecurityMockUser
        void whenAccountNameIsNotNullAndOver20() throws Exception {
            // given
            String accountName = "123456789012345678901";
            SpendingReq request = new SpendingReq(10000, -1L, SpendingCategory.FOOD, LocalDate.now(), accountName, "메모");
            given(spendingUseCase.createSpending(1L, request)).willReturn(SpendingSearchRes.Individual.builder().build());

            // when
            ResultActions result = performPostSpending(request);

            // then
            result.andDo(print()).andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("메모가 null이 아니면서 100자를 초과하는 경우 422 Unprocessable Entity를 반환한다.")
        @WithSecurityMockUser
        void whenMemoIsNotNullAndOver100() throws Exception {
            // given
            String memo = RandomStringUtils.random(101);
            SpendingReq request = new SpendingReq(10000, -1L, SpendingCategory.FOOD, LocalDate.now(), "소비처", memo);
            given(spendingUseCase.createSpending(1L, request)).willReturn(SpendingSearchRes.Individual.builder().build());

            // when
            ResultActions result = performPostSpending(request);

            // then
            result.andDo(print()).andExpect(status().isUnprocessableEntity());
        }

        private ResultActions performPostSpending(SpendingReq request) throws Exception {
            return mockMvc.perform(post("/v2/spendings")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)));
        }
    }
}
