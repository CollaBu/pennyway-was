package kr.co.pennyway.api.apis.ledger.controller;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.api.apis.ledger.usecase.TargetAmountUseCase;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TargetAmountController.class})
@ActiveProfiles("test")
public class TargetAmountControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TargetAmountUseCase targetAmountUseCase;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(post("/**").with(csrf()))
                .defaultRequest(put("/**").with(csrf()))
                .defaultRequest(delete("/**").with(csrf()))
                .build();
    }

    @Nested
    @DisplayName("당월 목표 금액 등록")
    class PostTargetAmount {
        @Test
        @DisplayName("오늘 날짜에 대한 요청이 아니면 400 Bad Request 에러 응답을 반환한다.")
        @WithMockUser
        void postTargetAmountNotThatMonth() throws Exception {
            // given
            int year = 2024;
            int month = 5;

            // when
            ResultActions result = performPostTargetAmount(year, month);

            // then
            result
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(TargetAmountErrorCode.INVALID_TARGET_AMOUNT_DATE.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(TargetAmountErrorCode.INVALID_TARGET_AMOUNT_DATE.getExplainError()));
        }

        private ResultActions performPostTargetAmount(int year, int month) throws Exception {
            return mockMvc.perform(post("/v2/target-amounts")
                    .param("year", String.valueOf(year))
                    .param("month", String.valueOf(month))
            );
        }
    }

    @Nested
    @DisplayName("당월 목표 금액 수정")
    class PutTargetAmount {
        @Test
        @DisplayName("amount가 null인 경우 422 Unprocessable Entity 에러 응답을 반환한다.")
        @WithMockUser
        void putTargetAmountWithInvalidAmountFormat() throws Exception {
            // when
            ResultActions result1 = performPutTargetAmount(1L, null);

            // then
            result1.andDo(print()).andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("amount가 0보다 작은 경우 422 Unprocessable Entity 에러 응답을 반환한다.")
        @WithSecurityMockUser
        void putTargetAmountWithNegativeAmount() throws Exception {
            // given
            Integer negativeAmount = -100000;

            // when
            ResultActions result = performPutTargetAmount(1L, negativeAmount);

            // then
            result.andDo(print()).andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("정상적인 요청이 들어왔을 때 200 OK 응답을 반환한다.")
        @WithSecurityMockUser
        void putTargetAmountWithValidRequest() throws Exception {
            // given
            int amount = 100000;
            given(targetAmountUseCase.updateTargetAmount(1L, amount)).willReturn(TargetAmountDto.TargetAmountInfo.from(null));

            // when
            ResultActions result = performPutTargetAmount(1L, amount);

            // then
            result.andDo(print()).andExpect(status().isOk());
        }


        private ResultActions performPutTargetAmount(Long targetAmountId, Integer amount) throws Exception {
            return mockMvc.perform(patch("/v2/target-amounts/{target_amount_id}", targetAmountId)
                    .param("amount", String.valueOf(amount))
            );
        }
    }
}
