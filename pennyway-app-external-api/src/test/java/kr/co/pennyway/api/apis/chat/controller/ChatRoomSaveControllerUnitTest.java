package kr.co.pennyway.api.apis.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.apis.chat.usecase.ChatRoomUseCase;
import kr.co.pennyway.api.config.fixture.ChatRoomFixture;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatRoomController.class)
@ActiveProfiles("test")
public class ChatRoomSaveControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatRoomUseCase chatRoomUseCase;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(MockMvcRequestBuilders.get("/**").with(csrf()))
                .build();
    }

    @Test
    @DisplayName("모두 올바른 값을 입력하면 채팅방 생성 요청에 성공한다.")
    @WithSecurityMockUser
    void createChatRoomSuccess() throws Exception {
        // given
        ChatRoom fixture = ChatRoomFixture.PRIVATE_CHAT_ROOM.toEntity(1L);
        ChatRoomReq.Create request = ChatRoomFixture.PRIVATE_CHAT_ROOM.toCreateRequest();
        given(chatRoomUseCase.createChatRoom(request, 1L)).willReturn(ChatRoomRes.Detail.of(fixture, null, true, 1, 10));

        // when
        ResultActions result = performPostChatRoom(request);

        // then
        result.andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("채팅 이미지가 null이어도 채팅방 생성 요청에 성공한다.")
    @WithSecurityMockUser
    void createChatRoomSuccessWithNullBackgroundImageUrl() throws Exception {
        // given
        ChatRoom fixture = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(1L);
        ChatRoomReq.Create request = ChatRoomFixture.PUBLIC_CHAT_ROOM.toCreateRequest();

        given(chatRoomUseCase.createChatRoom(request, 1L)).willReturn(ChatRoomRes.Detail.of(fixture, null, true, 1, 10));

        // when
        ResultActions result = performPostChatRoom(request);

        // then
        result.andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("delete/로 시작하지 않는 null이 아닌 채팅 이미지 URL이 입력되면 채팅방 생성 요청에 실패한다. (422 Unprocessable Entity)")
    @WithSecurityMockUser
    void createChatRoomFailWithInvalidBackgroundImageUrl() throws Exception {
        // given
        ChatRoomReq.Create request = new ChatRoomReq.Create("페니웨이", "짱짱", "1234", "invalid");

        // when
        ResultActions result = performPostChatRoom(request);

        // then
        result.andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    private ResultActions performPostChatRoom(ChatRoomReq.Create request) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post("/v2/chat-rooms")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));
    }
}
