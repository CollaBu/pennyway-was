package kr.co.pennyway.api.apis.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.api.apis.chat.usecase.ChatRoomUseCase;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatRoomController.class)
@ActiveProfiles("test")
public class CreateChatRoomControllerUnitTest {
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
    @DisplayName("채팅방 생성 성공")
    @WithSecurityMockUser
    void createChatRoomSuccess() throws Exception {
        // given
        String title = "테스트 채팅방";
        String description = "테스트 채팅방입니다.";
        String backgroundImageUrl = "https://test.com";
        Integer password = 123456;
        ChatRoomReq.Create request = new ChatRoomReq.Create(title, description, backgroundImageUrl, password);

        // when
        ResultActions result = performPostChatRoom(request);

        // then
        result.andDo(print())
                .andExpect(status().isOk());
    }

    private ResultActions performPostChatRoom(ChatRoomReq.Create request) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post("/v2/chat-rooms")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));
    }
}
