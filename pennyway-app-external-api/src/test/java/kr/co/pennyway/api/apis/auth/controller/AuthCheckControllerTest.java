package kr.co.pennyway.api.apis.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.auth.dto.AuthFindDto;
import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.usecase.AuthCheckUseCase;
import kr.co.pennyway.api.config.WebConfig;
import kr.co.pennyway.common.exception.StatusCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
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

import static kr.co.pennyway.common.exception.ReasonCode.REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthCheckController.class}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)})
@ActiveProfiles("local")
class AuthCheckControllerTest {
    private final String inputPhone = "010-1234-5678";
    private final String expectedUsername = "pennyway";
    private final String code = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthCheckUseCase authCheckUseCase;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(post("/**").with(csrf()))
                .build();
    }

    @Test
    @DisplayName("일반 회원의 휴대폰 번호로 아이디를 찾을 때 200 응답을 반환한다.")
    void findUsername() throws Exception {
        // given
        given(authCheckUseCase.findUsername(new PhoneVerificationDto.VerifyCodeReq(inputPhone, code))).willReturn(
                new AuthFindDto.FindUsernameRes(expectedUsername));

        // when
        ResultActions resultActions = findUsernameRequest(inputPhone, code);

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.username").value(expectedUsername));
    }

    @Test
    @DisplayName("일반 회원이 아닌 휴대폰 번호로 아이디를 찾을 때 404 응답을 반환한다.")
    void findUsernameIfUserNotFound() throws Exception {
        // given
        String phone = "010-1111-1111";
        given(authCheckUseCase.findUsername(new PhoneVerificationDto.VerifyCodeReq(phone, code))).willThrow(new UserErrorException(UserErrorCode.NOT_FOUND));

        // when
        ResultActions resultActions = findUsernameRequest(phone, code);

        // then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(UserErrorCode.NOT_FOUND.causedBy().getCode()))
                .andExpect(jsonPath("$.message").value(UserErrorCode.NOT_FOUND.getExplainError()));
    }

    @Test
    @DisplayName("휴대폰 번호와 코드를 입력하지 않았을 때 422 응답을 반환한다.")
    void findUsernameIfInputIsEmpty() throws Exception {
        // when
        ResultActions resultActions = findUsernameRequest("", "");

        // then
        resultActions
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value(
                        String.valueOf(StatusCode.UNPROCESSABLE_CONTENT.getCode() * 10 + REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY.getCode())))
                .andExpect(jsonPath("$.message").value(StatusCode.UNPROCESSABLE_CONTENT.name()));
    }

    private ResultActions findUsernameRequest(String phone, String code) throws Exception {
        return mockMvc.perform(get("/v1/find/username")
                .param("phone", phone)
                .param("code", code));
    }
}