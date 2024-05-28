package kr.co.pennyway.api.apis.storage.controller;

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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import kr.co.pennyway.api.apis.storage.dto.PresignedUrlDto;
import kr.co.pennyway.api.apis.storage.usecase.StorageUseCase;
import kr.co.pennyway.api.config.WebConfig;
import kr.co.pennyway.infra.common.exception.StorageErrorCode;
import kr.co.pennyway.infra.common.exception.StorageException;

@WebMvcTest(controllers = StorageController.class, excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)})
@ActiveProfiles("test")
class StorageControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private StorageUseCase storageUseCase;

	@BeforeEach
	void setUp(WebApplicationContext webApplicationContext) {
		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(webApplicationContext)
				.defaultRequest(post("/**").with(csrf()))
				.build();
	}

	@Test
	@DisplayName("Type이 PROFILE이고, UserId가 NULL일 때 400 응답을 반환한다.")
	void getPresignedUrlWithNullUserId() throws Exception {
		// given
		PresignedUrlDto.Req request = new PresignedUrlDto.Req("PROFILE", "jpg", null, null);
		given(storageUseCase.getPresignedUrl(request)).willThrow(new StorageException(StorageErrorCode.MISSING_REQUIRED_PARAMETER));

		// when
		ResultActions resultActions = getPresignedUrlRequest(request);

		// then
		resultActions.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Type이 CHAT이고, ChatroomId가 NULL일 때 400 응답을 반환한다.")
	void getPresignedUrlWithNullChatroomId() throws Exception {
		// given
		PresignedUrlDto.Req request = new PresignedUrlDto.Req("CHAT", "jpg", "userId", null);
		given(storageUseCase.getPresignedUrl(request)).willThrow(new StorageException(StorageErrorCode.MISSING_REQUIRED_PARAMETER));

		// when
		ResultActions resultActions = getPresignedUrlRequest(request);

		// then
		resultActions.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Type이 CHATROOM_PROFILE이고, ChatroomId가 NULL일 때 400 응답을 반환한다.")
	void getPresignedUrlWithNullChatroomIdForChatroomProfile() throws Exception {
		// given
		PresignedUrlDto.Req request = new PresignedUrlDto.Req("CHATROOM_PROFILE", "jpg", "userId", null);
		given(storageUseCase.getPresignedUrl(request)).willThrow(new StorageException(StorageErrorCode.MISSING_REQUIRED_PARAMETER));

		// when
		ResultActions resultActions = getPresignedUrlRequest(request);

		// then
		resultActions.andExpect(status().isBadRequest());
	}

	private ResultActions getPresignedUrlRequest(PresignedUrlDto.Req request) throws Exception {
		return mockMvc.perform(get("/v1/storage/presigned-url")
				.param("type", request.type())
				.param("ext", request.ext())
				.param("userId", request.userId())
				.param("chatRoomId", request.chatroomId()));
	}
}