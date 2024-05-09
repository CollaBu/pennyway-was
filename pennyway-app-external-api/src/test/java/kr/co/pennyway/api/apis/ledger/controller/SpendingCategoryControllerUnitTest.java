package kr.co.pennyway.api.apis.ledger.controller;

import kr.co.pennyway.api.apis.ledger.usecase.SpendingCategoryUseCase;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {SpendingCategoryController.class})
@ActiveProfiles("test")
public class SpendingCategoryControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpendingCategoryUseCase spendingCategoryUseCase;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(post("/**").with(csrf()))
                .build();
    }

    @Test
    @DisplayName("유효하지 않은 카테고리명을 입력하면 422 Unprocessable Entity 에러 응답을 반환한다.")
    @WithSecurityMockUser
    void postSpendingCategoryWithInvalidName() throws Exception {
        // given
        String icon = "FOOD";
        String whiteSpaceName = " ";
        String sixteenLengthName = "1234567890123456";

        // when
        ResultActions result1 = performPostSpendingCategory(whiteSpaceName, icon);
        ResultActions result2 = performPostSpendingCategory(sixteenLengthName, icon);

        // then
        result1.andDo(print()).andExpect(status().isUnprocessableEntity());
        result2.andDo(print()).andExpect(status().isUnprocessableEntity());
    }

    private ResultActions performPostSpendingCategory(String name, String icon) throws Exception {
        return mockMvc.perform(post("/v2/spending-categories")
                .param("name", name)
                .param("icon", icon));
    }
}
