package kr.co.pennyway.api.apis.ledger.controller;

import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.api.apis.ledger.usecase.SpendingCategoryUseCase;
import kr.co.pennyway.api.common.query.SpendingCategoryType;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    @DisplayName("type 쿼리 파라미터가 default, custom이면 요청에 성공한다. (대/소문자 모두 허용)")
    void patchSpendingCategoryWithValidType() throws Exception {
        // given
        Long spendingCategoryId = 1L;
        given(spendingCategoryUseCase.updateSpendingCategory(anyLong(), spendingCategoryId, any())).willReturn(new SpendingCategoryDto.Res(false, 1L, "name", SpendingCategory.FOOD));

        // when
        ResultActions resultDefault = performPatchSpendingCategory(spendingCategoryId, "DEFAULT");
        ResultActions resultCustom = performPatchSpendingCategory(spendingCategoryId, "custom");

        // then
        resultDefault.andExpect(status().isOk());
        resultCustom.andExpect(status().isOk());
    }

    @Test
    @DisplayName("type이 default면서, categoryId가 0(CUSTOM) 혹은 12(OTHER)이면 400 Bad Request 에러 응답을 반환한다.")
    void patchSpendingCategoryWithInvalidDefaultAndCategoryId() throws Exception {
        // given
        Long categoryId1 = 0L, categoryId2 = 12L;
        SpendingCategoryType type = SpendingCategoryType.DEFAULT;

        // when
        ResultActions result1 = performPatchSpendingCategory(categoryId1, type.name());
        ResultActions result2 = performPatchSpendingCategory(categoryId2, type.name());

        // then
        result1.andExpect(status().isBadRequest());
        result2.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("type이 default 혹은 custom이 아닌 경우 422 Unprocessable Entity 에러 응답을 반환한다.")
    void patchSpendingCategoryWithInvalidType() throws Exception {
        // given
        Long spendingCategoryId = 1L;

        // when
        ResultActions result = performPatchSpendingCategory(spendingCategoryId, "invalid");

        // then
        result.andExpect(status().isUnprocessableEntity());
    }

    private ResultActions performPatchSpendingCategory(Long spendingCategoryId, String type) throws Exception {
        return mockMvc.perform(patch("/v2/spending-categories/{categoryId}", spendingCategoryId)
                .param("type", type));
    }
}
