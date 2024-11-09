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

        log.info("saved messages: {}", chatMessageRepository.findRecentMessages(chatRoom.getId(), 50));

        // when
        ResponseEntity<?> response = performRequest(user, chatRoom.getId(), messages.get(49).getChatId(), 30);

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> {
                    SliceResponseTemplate<ChatRes.ChatDetail> slice = extractChatDetail(response);

                    assertThat(slice.contents()).hasSize(30);
                    assertThat(slice.hasNext()).isTrue();
                }
        );
    }

    private ResponseEntity<?> performRequest(User user, Long chatRoomId, Long lastMessageId, int size) {
        RequestParameters parameters = RequestParameters.defaultGet(BASE_URL)
                .user(user)
                .queryParams(RequestParameters.createQueryParams(
                        "lastMessageId", 1000L,
                        "size", 30
                ))
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
