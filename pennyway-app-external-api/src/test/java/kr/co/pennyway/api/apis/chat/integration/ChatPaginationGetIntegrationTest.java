package kr.co.pennyway.api.apis.chat.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.chat.dto.ChatRes;
import kr.co.pennyway.api.common.response.SliceResponseTemplate;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.util.ApiTestHelper;
import kr.co.pennyway.api.common.util.RequestParameters;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.ChatMemberFixture;
import kr.co.pennyway.api.config.fixture.ChatRoomFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessage;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessageBuilder;
import kr.co.pennyway.domain.common.redis.message.repository.ChatMessageRepository;
import kr.co.pennyway.domain.common.redis.message.type.MessageCategoryType;
import kr.co.pennyway.domain.common.redis.message.type.MessageContentType;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.repository.ChatRoomRepository;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.repository.ChatMemberRepository;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.repository.UserRepository;
import kr.co.pennyway.infra.client.guid.IdGenerator;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ExternalApiIntegrationTest
public class ChatPaginationGetIntegrationTest extends ExternalApiDBTestConfig {
    private static final String BASE_URL = "/v2/chat-rooms/{chatRoomId}/chats";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private JwtProvider accessTokenProvider;

    @Autowired
    private IdGenerator<Long> idGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private ApiTestHelper apiTestHelper;

    @BeforeEach
    void setUp() {
        apiTestHelper = new ApiTestHelper(restTemplate, objectMapper, accessTokenProvider);
    }

    @AfterEach
    void tearDown() {
        Set<String> keys = redisTemplate.keys("chatroom:*:message");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("채팅방의 이전 메시지들을 정상적으로 페이징하여 조회할 수 있다")
    void successReadChats() {
        // given
        User user = createUser();
        ChatRoom chatRoom = createChatRoom();
        createChatMember(user, chatRoom, ChatMemberRole.ADMIN);
        List<ChatMessage> messages = setupTestMessages(chatRoom.getId(), user.getId(), 50);

        // when
        ResponseEntity<?> response = performRequest(user, chatRoom.getId(), messages.get(49).getChatId(), 30);

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> {
                    SliceResponseTemplate<ChatRes.ChatDetail> slice = extractChatDetail(response);

                    assertEquals(messages.get(48).getChatId(), slice.contents().get(0).chatId(), "lastMessageId에 해당하는 메시지는 포함되지 않아야 합니다");
                    assertThat(slice.contents()).hasSize(30);
                    assertThat(slice.hasNext()).isTrue();
                }
        );
    }

    @Test
    @DisplayName("채팅방 멤버가 아닌 사용자는 메시지를 조회할 수 없다")
    void readChatsWithoutPermissionTest() {
        // given
        User nonMember = createUser();
        ChatRoom chatRoom = createChatRoom();

        // when
        ResponseEntity<?> response = performRequest(nonMember, chatRoom.getId(), 0L, 30);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("존재하지 않는 채팅방의 메시지는 조회할 수 없다")
    void readChatsFromNonExistentRoomTest() {
        // given
        User user = createUser();
        Long nonExistentRoomId = 9999L;

        // when
        ResponseEntity<?> response = performRequest(user, nonExistentRoomId, 0L, 30);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("마지막 페이지를 조회할 때 hasNext는 false여야 한다")
    void readLastPageTest() {
        // given
        User user = createUser();
        ChatRoom chatRoom = createChatRoom();
        createChatMember(user, chatRoom, ChatMemberRole.ADMIN);
        List<ChatMessage> messages = setupTestMessages(chatRoom.getId(), user.getId(), 10);

        // when
        ResponseEntity<?> response = performRequest(user, chatRoom.getId(), messages.get(0).getChatId(), 10);

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> {
                    SliceResponseTemplate<ChatRes.ChatDetail> slice = extractChatDetail(response);

                    assertThat(slice.contents()).hasSize(0);
                    assertThat(slice.hasNext()).isFalse();
                }
        );
    }

    @Test
    @DisplayName("메시지가 없는 채팅방을 조회하면 빈 리스트를 반환해야 한다")
    void readEmptyChatsTest() {
        // given
        User user = createUser();
        ChatRoom chatRoom = createChatRoom();
        createChatMember(user, chatRoom, ChatMemberRole.ADMIN);

        // when
        ResponseEntity<?> response = performRequest(user, chatRoom.getId(), Long.MAX_VALUE, 30);

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> {
                    SliceResponseTemplate<ChatRes.ChatDetail> slice = extractChatDetail(response);

                    assertThat(slice.contents()).isEmpty();
                    assertThat(slice.hasNext()).isFalse();
                }
        );
    }

    private ResponseEntity<?> performRequest(User user, Long chatRoomId, Long lastMessageId, int size) {
        RequestParameters parameters = RequestParameters.defaultGet(BASE_URL)
                .user(user)
                .queryParams(RequestParameters.createQueryParams("lastMessageId", lastMessageId, "size", size))
                .uriVariables(new Object[]{chatRoomId})
                .build();

        return apiTestHelper.callApi(
                parameters,
                new TypeReference<SuccessResponse<Map<String, SliceResponseTemplate<ChatRes.ChatDetail>>>>() {
                }
        );
    }

    private User createUser() {
        return userRepository.save(UserFixture.GENERAL_USER.toUser());
    }

    private ChatRoom createChatRoom() {
        return chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity(idGenerator.generate()));
    }

    private ChatMember createChatMember(User user, ChatRoom chatRoom, ChatMemberRole role) {
        return switch (role) {
            case ADMIN -> chatMemberRepository.save(ChatMemberFixture.ADMIN.toEntity(user, chatRoom));
            case MEMBER -> chatMemberRepository.save(ChatMemberFixture.MEMBER.toEntity(user, chatRoom));
        };
    }

    private List<ChatMessage> setupTestMessages(Long chatRoomId, Long senderId, int count) {
        List<ChatMessage> messages = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            ChatMessage message = ChatMessageBuilder.builder()
                    .chatRoomId(chatRoomId)
                    .chatId(idGenerator.generate())
                    .content("Test message " + i)
                    .contentType(MessageContentType.TEXT)
                    .categoryType(MessageCategoryType.NORMAL)
                    .sender(senderId)
                    .build();

            messages.add(chatMessageRepository.save(message));
        }

        return messages;
    }

    private SliceResponseTemplate<ChatRes.ChatDetail> extractChatDetail(ResponseEntity<?> response) {
        SliceResponseTemplate<ChatRes.ChatDetail> slice = null;

        try {
            SuccessResponse<Map<String, SliceResponseTemplate<ChatRes.ChatDetail>>> successResponse = (SuccessResponse<Map<String, SliceResponseTemplate<ChatRes.ChatDetail>>>) response.getBody();
            slice = successResponse.getData().get("chats");
        } catch (Exception e) {
            fail("응답 데이터 추출에 실패했습니다.", e);
        }

        return slice;
    }
}
