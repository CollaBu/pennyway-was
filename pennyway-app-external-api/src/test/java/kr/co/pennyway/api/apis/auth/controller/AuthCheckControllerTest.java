package kr.co.pennyway.api.apis.auth.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.co.pennyway.api.apis.auth.dto.AuthFindDto;
import kr.co.pennyway.api.apis.auth.mapper.AuthFindMapper;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;

@ExternalApiIntegrationTest
@AutoConfigureMockMvc
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class AuthCheckControllerTest extends ExternalApiDBTestConfig {
	private final String inputPhone = "010-1234-5678";
	private final String expectedUsername = "pennyway";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthFindMapper authFindMapper;

	@Test
	@DisplayName("[x] 아이디 찾기 검증 테스트")
	void findUsername() throws Exception {
		// given
		given(authFindMapper.findUsername(inputPhone)).willReturn(new AuthFindDto.FindUsernameRes(expectedUsername));

		// when
		ResultActions resultActions = findUsernameRequest(inputPhone);

		System.out.println(resultActions.andReturn().getResponse().getContentAsString());

		// then
		resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.user.username").value(expectedUsername));
	}

	private ResultActions findUsernameRequest(String phone) throws Exception {
		return mockMvc.perform(get("/v1/find/username")
				.param("phone", phone));
	}
}