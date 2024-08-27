package kr.co.pennyway.api.apis.ledger.controller;

import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.api.apis.ledger.usecase.SpendingCategoryUseCase;
import kr.co.pennyway.api.common.query.SpendingCategoryType;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SpendingCategoryController.class)
@ActiveProfiles("test")
public class GetSpendingsByCategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpendingCategoryUseCase spendingCategoryUseCase;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(MockMvcRequestBuilders.get("/**").with(csrf()))
                .build();
    }

    @Test
    @DisplayName("default, custom 타입은 올바르게 조회된다.")
    @WithSecurityMockUser
    void getSpendingsByCategory() throws Exception {
        given(spendingCategoryUseCase.getSpendingsByCategory(any(), any(), any(), any())).willReturn(new SpendingSearchRes.MonthSlice(new ArrayList<>(), 0, 0, 0, false));

        performGetSpendingsByCategory(1L, SpendingCategoryType.DEFAULT.name())
                .andDo(print())
                .andExpect(status().isOk());
        performGetSpendingsByCategory(1L, SpendingCategoryType.CUSTOM.name().toLowerCase())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("잘못된 타입을 조회하면 422 에러가 발생한다.")
    void getSpendingsByCategory_InvalidType() throws Exception {
        performGetSpendingsByCategory(1L, "invalid")
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("카테고리 타입이 default이면서 categoryId가 0이거나 12이면 400 에러가 발생한다.")
    void getSpendingsByCategory_InvalidCategoryId() throws Exception {
        performGetSpendingsByCategory(0L, SpendingCategoryType.DEFAULT.name())
                .andDo(print())
                .andExpect(status().isBadRequest());
        performGetSpendingsByCategory(12L, SpendingCategoryType.DEFAULT.name())
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private ResultActions performGetSpendingsByCategory(Long categoryId, String type) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get("/v2/spending-categories/{categoryId}/spendings", categoryId)
                .param("type", type));
    }
}
