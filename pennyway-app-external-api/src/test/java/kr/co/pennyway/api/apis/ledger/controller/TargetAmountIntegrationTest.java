package kr.co.pennyway.api.apis.ledger.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import kr.co.pennyway.domain.domains.target.service.TargetAmountService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExternalApiIntegrationTest
@AutoConfigureMockMvc
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class TargetAmountIntegrationTest extends ExternalApiDBTestConfig {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserService userService;

	@Autowired
	private TargetAmountService targetAmountService;

	@Nested
	@Order(1)
	@DisplayName("당월 목표 금액 등록/수정")
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class PutTargetAmount {
		@Order(1)
		@Test
		@DisplayName("당월 목표 금액 entity가 존재하지 않을 경우 새로 생성한다.")
		@WithSecurityMockUser
		@Transactional
		void putTargetAmountNotFound() throws Exception {
			// given
			User user = UserFixture.GENERAL_USER.toUser();
			userService.createUser(user);
			String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

			// when
			ResultActions result = performPutTargetAmount(date, 100000, user);

			// then
			result.andExpect(status().isOk());
			assertNotNull(targetAmountService.readTargetAmountThatMonth(user.getId(), LocalDate.now()).orElse(null));
		}

		@Order(2)
		@Test
		@DisplayName("당월 목표 금액 entity가 존재하는 경우 amount를 수정한다.")
		@WithSecurityMockUser
		@Transactional
		void putTargetAmountFound() throws Exception {
			// given
			User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
			TargetAmount targetAmount = targetAmountService.createTargetAmount(TargetAmount.of(100000, user));

			String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

			// when
			ResultActions result = performPutTargetAmount(date, 200000, user);

			// then
			result.andExpect(status().isOk());
			assertEquals(200000, targetAmount.getAmount());
		}

		private ResultActions performPutTargetAmount(String date, Integer amount, User requestUser) throws Exception {
			UserDetails userDetails = SecurityUserDetails.from(requestUser);
			return mockMvc.perform(put("/v2/targets")
					.with(user(userDetails))
					.param("date", date)
					.param("amount", amount.toString()));
		}
	}
}
