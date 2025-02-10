package kr.co.pennyway.api.apis.storage.controller;

import kr.co.pennyway.api.apis.storage.usecase.StorageUseCase;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StorageController.class)
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
    @WithSecurityMockUser
    @DisplayName("jpg, png, jpeg 이외의 확장자로 요청 시 422 응답을 반환한다.")
    void getPresignedUrlWithInvalidExt() throws Exception {
        // when
        ResultActions resultActions = getPresignedUrlRequest(ObjectKeyType.PROFILE, "gif", null);

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithSecurityMockUser
    @DisplayName("유효하지 않은 Type으로 요청 시 422 응답을 반환한다.")
    void getPresignedUrlWithInvalidType() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(get("/v1/storage/presigned-url")
                .param("type", "INVALID")
                .param("ext", "jpg"));

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithSecurityMockUser
    @DisplayName("Type이 CHAT이고, ChatroomId가 NULL일 때 422 응답을 반환한다.")
    void getPresignedUrlWithNullChatroomIdForChat() throws Exception {
        // when
        ResultActions resultActions = getPresignedUrlRequest(ObjectKeyType.CHAT, "jpg", null);

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithSecurityMockUser
    @DisplayName("올바른 Profile 파라미터로 요청 시 200 응답을 반환한다.")
    void getPresignedUrlWithValidProfileParameters() throws Exception {
        // when
        ResultActions resultActions = getPresignedUrlRequest(ObjectKeyType.PROFILE, "jpg", null);

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithSecurityMockUser
    @DisplayName("올바른 Chat 파라미터로 요청 시 200 응답을 반환한다.")
    void getPresignedUrlWithValidChatParameters() throws Exception {
        // when
        ResultActions resultActions = getPresignedUrlRequest(ObjectKeyType.CHAT, "jpg", 1L);

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithSecurityMockUser
    @DisplayName("올바른 ChatroomProfile 파라미터로 요청 시 200 응답을 반환한다.")
    void getPresignedUrlWithValidChatroomProfileParameters() throws Exception {
        // when
        ResultActions resultActions = getPresignedUrlRequest(ObjectKeyType.CHATROOM_PROFILE, "jpg", null);

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithSecurityMockUser
    @DisplayName("올바른 ChatProfile 파라미터로 요청 시 200 응답을 반환한다.")
    void getPresignedUrlWithValidChatProfileParameters() throws Exception {
        // when
        ResultActions resultActions = getPresignedUrlRequest(ObjectKeyType.CHAT_PROFILE, "jpg", 1L);

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithSecurityMockUser
    @DisplayName("올바른 Feed 파라미터로 요청 시 200 응답을 반환한다.")
    void getPresignedUrlWithValidFeedParameters() throws Exception {
        // when
        ResultActions resultActions = getPresignedUrlRequest(ObjectKeyType.FEED, "jpg", null);

        // then
        resultActions.andExpect(status().isOk());
    }

    private ResultActions getPresignedUrlRequest(ObjectKeyType type, String ext, Long chatroomId) throws Exception {
        return mockMvc.perform(get("/v1/storage/presigned-url")
                .param("type", type.name())
                .param("ext", ext)
                .param("chatroomId", chatroomId != null ? chatroomId.toString() : null));
    }
}