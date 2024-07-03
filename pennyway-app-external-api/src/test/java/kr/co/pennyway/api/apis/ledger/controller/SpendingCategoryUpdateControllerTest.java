package kr.co.pennyway.api.apis.ledger.controller;

import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.api.apis.ledger.usecase.SpendingCategoryUseCase;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import kr.co.pennyway.domain.common.redis.sign.SignEventLogService;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpendingCategoryController.class)
@ActiveProfiles("test")
public class SpendingCategoryUpdateControllerTest {
    @MockBean
    private SignEventLogService signEventLogService;
    @MockBean
    private JwtProvider accessTokenProvider;
    @MockBean
    private SpendingCategoryUseCase spendingCategoryUseCase;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .defaultRequest(patch("/**").with(csrf()))
                .build();
    }

    @Test
    @DisplayName("icon이 8자 이하의 공백이 아닌 문자열이고, icon이 SpendingCategory에 있는 값이면 200 OK 응답을 반환한다.")
    @WithSecurityMockUser
    void patchSpendingCategorySuccess() throws Exception {
        // given
        Long spendingCategoryId = 1L;
        String expectedName = "name";
        SpendingCategory icon = SpendingCategory.FOOD;
        given(spendingCategoryUseCase.updateSpendingCategory(1L, spendingCategoryId, expectedName, icon)).willReturn(new SpendingCategoryDto.Res(false, -1L, "name", icon));

        // when
        ResultActions resultDefault = performPatchSpendingCategory(spendingCategoryId, expectedName, icon.name());

        // then
        resultDefault.andExpect(status().isOk());
    }

    @Test
    @DisplayName("name이 공백 문자거나, 8자 이상인 경우 422 Unprocessable Entity 에러 응답을 반환한다.")
    void patchSpendingCategoryWithInvalidDefaultAndCategoryId() throws Exception {
        // given
        Long spendingCategoryId = 1L;
        String whitespaceName = "  ", longName = "123456789";
        SpendingCategory icon = SpendingCategory.FOOD;

        // when
        ResultActions result1 = performPatchSpendingCategory(spendingCategoryId, whitespaceName, icon.name());
        ResultActions result2 = performPatchSpendingCategory(spendingCategoryId, longName, icon.name());

        // then
        result1.andExpect(status().isUnprocessableEntity());
        result2.andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("icon이 SpendingCategory에 없는 값이면 422 Unprocessable Entity 에러 응답을 반환한다.")
    void patchSpendingCategoryWithInvalidIcon() throws Exception {
        // given
        Long spendingCategoryId = 1L;
        String name = "name", invalidIcon = "INVALID";

        // when
        ResultActions result = performPatchSpendingCategory(spendingCategoryId, name, invalidIcon);

        // then
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("icon이 CUSTOM이면 400 Bad Request 에러 응답을 반환한다.")
    void patchSpendingCategoryWithCustomIcon() throws Exception {
        // given
        Long spendingCategoryId = 1L;
        String name = "name", customIcon = SpendingCategory.CUSTOM.name();

        // when
        ResultActions result = performPatchSpendingCategory(spendingCategoryId, name, customIcon);

        // then
        result.andExpect(status().isBadRequest());
    }

    private ResultActions performPatchSpendingCategory(Long spendingCategoryId, String name, String icon) throws Exception {
        return mockMvc.perform(patch("/v2/spending-categories/{categoryId}", spendingCategoryId)
                .param("name", name)
                .param("icon", icon));
    }
}
