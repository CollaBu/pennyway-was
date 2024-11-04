package kr.co.pennyway.api.apis.chat.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.chat.dto.ChatMemberRes;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.util.ApiTestHelper;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.ChatRoomFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomService;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.service.ChatMemberService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ExternalApiIntegrationTest
public class ChatMemberBatchGetIntegrationTest extends ExternalApiDBTestConfig {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ChatMemberService chatMemberService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProvider accessTokenProvider;

    @LocalServerPort
    private int port;

    private ApiTestHelper apiTestHelper;

    private User owner;
    private ChatRoom chatRoom;
    private ChatMember ownerMember;

    @BeforeEach
    void setUp() {
        apiTestHelper = new ApiTestHelper(restTemplate, objectMapper, accessTokenProvider);

        owner = userService.createUser(UserFixture.GENERAL_USER.toUser());
        chatRoom = chatRoomService.create(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(1L));
        ownerMember = chatMemberService.createAdmin(owner, chatRoom);
    }

    @Test
    @DisplayName("채팅방 멤버 조회를 성공한다")
    void successReadChatMembers() {
        // given
        List<ChatMember> members = createTestMembers(10);
        List<Long> memberIds = members.stream().map(ChatMember::getId).toList();

        // when
        ResponseEntity<?> response = apiTestHelper.callApi(
                "http://localhost:" + port + "/v2/chat-rooms/{chatRoomId}/chat-members?ids={ids}",
                HttpMethod.GET,
                owner,
                null,
                new TypeReference<SuccessResponse<Map<String, List<ChatMemberRes.MemberDetail>>>>() {
                },
                chatRoom.getId(),
                String.join(",", memberIds.stream().map(String::valueOf).toList())
        );
        SuccessResponse<Map<String, List<ChatMemberRes.MemberDetail>>> body = (SuccessResponse<Map<String, List<ChatMemberRes.MemberDetail>>>) response.getBody();
        List<ChatMemberRes.MemberDetail> payload = body.getData().get("chatMembers");

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(memberIds.size(), payload.size());
    }

    private List<ChatMember> createTestMembers(int count) {
        List<ChatMember> createdMembers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            User member = userService.createUser(UserFixture.GENERAL_USER.toUser());
            createdMembers.add(chatMemberService.createMember(member, chatRoom));
        }
        return createdMembers;
    }
}
