package kr.co.pennyway.api.apis.ledger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.ledger.usecase.TargetAmountUseCase;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import kr.co.pennyway.domain.domains.target.exception.TargetAmountErrorCode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TargetAmountController.class})
@ActiveProfiles("test")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class TargetAmountControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Order(1)
    @Nested
    @DisplayName("당월 목표 금액 등록/수정")
    class PutTargetAmount {
        @Test
        @DisplayName("date가 'yyyy-MM-dd' 형식이 아닐 경우 422 Unprocessable Entity 에러 응답을 반환한다.")
        @WithMockUser
        void putTargetAmountWithInvalidDateFormat() throws Exception {
            // given
            String date = "2024/05/08";
            Integer amount = 100000;

            // when
            ResultActions result = performPutTargetAmount(date, amount);

            // then
            result
                    .andDo(print())
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("date가 null인 경우 422 Unprocessable Entity 에러 응답을 반환한다.")
        @WithMockUser
        void putTargetAmountWithNullDate() throws Exception {
            // given
            Integer amount = 100000;

            // when
            ResultActions result = performPutTargetAmount(null, amount);

            // then
            result
                    .andDo(print())
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("date가 당월 날짜가 아닌 경우 400 Bad Request 에러 응답을 반환한다.")
        @WithMockUser
        void putTargetAmountWithInvalidDate() throws Exception {
            // given
            String date = "1999-05-19";
            Integer amount = 100000;

            // when
            ResultActions result = performPutTargetAmount(date, amount);

            // then
            result
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(TargetAmountErrorCode.INVALID_TARGET_AMOUNT_DATE.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(TargetAmountErrorCode.INVALID_TARGET_AMOUNT_DATE.getExplainError()));
        }

        @Test
        @DisplayName("amount가 null인 경우 422 Unprocessable Entity 에러 응답을 반환한다.")
        @WithMockUser
        void putTargetAmountWithInvalidAmountFormat() throws Exception {
            // given
            String date = "2024-05-08";

            // when
            ResultActions result1 = performPutTargetAmount(date, null);

            // then
            result1
                    .andDo(print())
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("amount가 0보다 작은 경우 422 Unprocessable Entity 에러 응답을 반환한다.")
        @WithMockUser
        void putTargetAmountWithNegativeAmount() throws Exception {
            // given
            String date = "2024-05-08";
            Integer negativeAmount = -100000;

            // when
            ResultActions result = performPutTargetAmount(date, negativeAmount);

            // then
            result
                    .andDo(print())
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("정상적인 요청이 들어왔을 때 200 OK 응답을 반환한다.")
        @WithSecurityMockUser
        void putTargetAmountWithValidRequest() throws Exception {
            // given
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Integer amount = 100000;

            // when
            ResultActions result = performPutTargetAmount(date, amount);

            // then
            result
                    .andDo(print())
                    .andExpect(status().isOk());
        }


        private ResultActions performPutTargetAmount(String date, Integer amount) throws Exception {
            return mockMvc.perform(put("/v2/targets")
                    .param("date", date)
                    .param("amount", String.valueOf(amount))
            );
        }
    }
}
