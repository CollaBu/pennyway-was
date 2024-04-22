package kr.co.pennyway.api.apis.auth.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.co.pennyway.api.apis.auth.dto.AuthFindDto;
import kr.co.pennyway.api.apis.auth.usecase.AuthCheckUseCase;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;

@WebMvcTest(controllers = {AuthCheckController.class})
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
		given(authCheckUseCase.findUsername(inputPhone, code)).willReturn(new AuthFindDto.FindUsernameRes(expectedUsername));

		// when
		ResultActions resultActions = findUsernameRequest(inputPhone);

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
		given(authCheckUseCase.findUsername(phone, code)).willThrow(new UserErrorException(UserErrorCode.NOT_FOUND));

		// when
		ResultActions resultActions = findUsernameRequest(phone);

		// then
		resultActions
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value(UserErrorCode.NOT_FOUND.causedBy().getCode()))
				.andExpect(jsonPath("$.message").value(UserErrorCode.NOT_FOUND.getExplainError()));
	}

	private ResultActions findUsernameRequest(String phone) throws Exception {
		return mockMvc.perform(get("/v1/find/username")
				.param("phone", phone));
	}
}