package kr.co.pennyway.api.apis.ledge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TargetAmountController.class})
@ActiveProfiles("test")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class TargetAmountControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        @DisplayName("date가 yyyy-MM 형식이 아닐 경우 422 Unprocessable Entity 에러 응답을 반환한다.")
        @WithMockUser
        void putTargetAmount() throws Exception {
            // given
            String date = "2024/05/08";
            Integer amount = null;

            // when
            ResultActions result = performPutTargetAmount(date, amount);

            // then
            result
                    .andDo(print())
                    .andExpect(status().isUnprocessableEntity());
        }

        private ResultActions performPutTargetAmount(String date, Integer amount) throws Exception {
            return mockMvc.perform(put("/v2/targets")
                    .param("date", date)
                    .param("amount", String.valueOf(amount))
            );
        }
    }
}
