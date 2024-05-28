package kr.co.pennyway.api.apis.ledger.controller;

import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.api.apis.ledger.usecase.SpendingCategoryUseCase;
import kr.co.pennyway.api.config.WebConfig;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import kr.co.pennyway.domain.domains.spending.dto.CategoryInfo;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorCode;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {SpendingCategoryController.class}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)})
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

    @Test
    @DisplayName("유효하지 않은 아이콘을 입력하면 422 Unprocessable Entity 에러 응답을 반환한다.")
    @WithSecurityMockUser
    void postSpendingCategoryWithInvalidIcon() throws Exception {
        // given
        String name = "식비";
        String whiteSpaceIcon = " ";
        String invalidIcon = "INVALID";
        String lowerCaseIcon = "food";

        // when
        ResultActions result1 = performPostSpendingCategory(name, whiteSpaceIcon);
        ResultActions result2 = performPostSpendingCategory(name, invalidIcon);
        ResultActions result3 = performPostSpendingCategory(name, lowerCaseIcon);

        // then
        result1.andDo(print()).andExpect(status().isUnprocessableEntity());
        result2.andDo(print()).andExpect(status().isUnprocessableEntity());
        result3.andDo(print()).andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("OTHER 아이콘을 입력하면 400 BAD_REQUEST 에러 응답을 반환한다.")
    @WithSecurityMockUser
    void postSpendingCategoryWithOtherIcon() throws Exception {
        // given
        String name = "식비";
        String icon = "OTHER";

        // when
        ResultActions result = performPostSpendingCategory(name, icon);

        // then
        result
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(SpendingErrorCode.INVALID_ICON.causedBy().getCode()))
                .andExpect(jsonPath("$.message").value(SpendingErrorCode.INVALID_ICON.getExplainError()));
    }


    @Test
    @DisplayName("카테고리명과 아이콘을 입력하면 200 OK 응답을 반환한다.")
    @WithSecurityMockUser
    void postSpendingCategory() throws Exception {
        // given
        String name = "식비";
        String icon = "FOOD";
        given(spendingCategoryUseCase.createSpendingCategory(any(), any(), any())).willReturn(SpendingCategoryDto.Res.from(CategoryInfo.of(1L, name, SpendingCategory.FOOD)));

        // when
        ResultActions result = performPostSpendingCategory(name, icon);

        // then
        result.andDo(print()).andExpect(status().isOk());
    }

    private ResultActions performPostSpendingCategory(String name, String icon) throws Exception {
        return mockMvc.perform(post("/v2/spending-categories")
                .param("name", name)
                .param("icon", icon));
    }
}
