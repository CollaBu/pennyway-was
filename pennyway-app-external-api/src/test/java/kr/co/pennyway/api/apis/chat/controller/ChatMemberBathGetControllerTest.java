package kr.co.pennyway.api.apis.chat.controller;

import kr.co.pennyway.api.apis.chat.dto.ChatMemberRes;
import kr.co.pennyway.api.apis.chat.usecase.ChatMemberUseCase;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatMemberController.class)
@ActiveProfiles("test")
public class ChatMemberBathGetControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatMemberUseCase chatMemberUseCase;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(MockMvcRequestBuilders.get("/**").with(csrf()))
                .build();
    }

    @Test
    @DisplayName("채팅방 멤버 조회에 성공한다")
    @WithSecurityMockUser
    void successReadChatMembers() throws Exception {
        // given
        Long chatRoomId = 1L;
        Set<Long> memberIds = Set.of(1L, 2L, 3L);
        List<ChatMemberRes.MemberDetail> expectedResponse = createMockMemberDetails();

        given(chatMemberUseCase.readChatMembers(chatRoomId, memberIds)).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/v2/chat-rooms/{chatRoomId}/chat-members", chatRoomId)
                        .param("ids", "1,2,3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.chatMembers").isArray())
                .andExpect(jsonPath("$.data.chatMembers.length()").value(3))
                .andDo(print());
    }

    @Test
    @DisplayName("50개를 초과하는 멤버 ID 요청 시 실패한다")
    @WithSecurityMockUser
    void failReadChatMembersWhenExceedLimit() throws Exception {
        // given
        Long chatRoomId = 1L;
        Set<Long> memberIds = LongStream.rangeClosed(1, 51)
                .boxed()
                .collect(Collectors.toSet());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/v2/chat-rooms/{chatRoomId}/chat-members", chatRoomId)
                        .param("ids", memberIds.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(",")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("ids가 null인 경우 실패한다 <400 Bad Request>")
    @WithSecurityMockUser
    void failReadChatMembersWhenIdsIsNull() throws Exception {
        // given
        Long chatRoomId = 1L;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/v2/chat-rooms/{chatRoomId}/chat-members", chatRoomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("ids가 빈 값일 경우 실패한다")
    @WithSecurityMockUser
    void failReadChatMembersWhenIdsIsEmpty() throws Exception {
        // given
        Long chatRoomId = 1L;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/v2/chat-rooms/{chatRoomId}/chat-members", chatRoomId)
                        .param("ids", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    private List<ChatMemberRes.MemberDetail> createMockMemberDetails() {
        return List.of(
                new ChatMemberRes.MemberDetail(1L, 2L, "User1", ChatMemberRole.MEMBER, null, LocalDateTime.now(), null),
                new ChatMemberRes.MemberDetail(2L, 3L, "User2", ChatMemberRole.MEMBER, null, LocalDateTime.now(), null),
                new ChatMemberRes.MemberDetail(3L, 4L, "User3", ChatMemberRole.MEMBER, null, LocalDateTime.now(), null)
        );
    }
}
