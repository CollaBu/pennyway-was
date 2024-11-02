package kr.co.pennyway.domain.common.redis.message.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessage;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessageBuilder;
import kr.co.pennyway.domain.common.redis.message.type.MessageCategoryType;
import kr.co.pennyway.domain.common.redis.message.type.MessageContentType;
import kr.co.pennyway.domain.config.ContainerRedisTestConfig;
import kr.co.pennyway.domain.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ContextConfiguration(classes = {RedisConfig.class})
@DataRedisTest(properties = "spring.config.location=classpath:application-domain.yml")
@Import({ChatMessageRepositoryImpl.class})
@ActiveProfiles("test")
public class ChatMessageRepositoryImplTest extends ContainerRedisTestConfig {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    //    @Autowired
    private ChatMessageRepositoryImpl chatMessageRepositoryImpl;

    private ChatMessage chatMessage;

    @BeforeEach
    void setUp() {
        chatMessageRepositoryImpl = new ChatMessageRepositoryImpl(redisTemplate, objectMapper);
        chatMessage = ChatMessageBuilder.builder()
                .chatRoomId(1L)
                .chatId(1L)
                .content("Hello")
                .contentType(MessageContentType.TEXT)
                .categoryType(MessageCategoryType.NORMAL)
                .sender(1L)
                .build();
    }

    @Test
    @DisplayName("Happy Path: 채팅 메시지 저장에 성공한다")
    void successSaveChatMessage() {
        // when
        chatMessageRepositoryImpl.save(chatMessage);

        // then
        List<ChatMessage> messages = chatMessageRepositoryImpl.findRecentMessages(1L, 1);
        assertFalse(messages.isEmpty(), "저장된 메시지는 조회할 수 있어야 합니다");
    }

    @Test
    @DisplayName("최근 메시지를 지정한 개수만큼 조회한다")
    void successFindRecentMessages() {
        // given
        saveMessagesInOrder(1L, 5);

        // when
        List<ChatMessage> messages = chatMessageRepositoryImpl.findRecentMessages(1L, 3);

        // then
        assertAll(
                () -> assertEquals(3, messages.size(), "요청한 개수만큼 메시지가 조회되어야 합니다"),
                () -> assertEquals("Message 5", messages.get(0).getContent(), "최신 메시지가 먼저 조회되어야 합니다"),
                () -> assertEquals("Message 4", messages.get(1).getContent()),
                () -> assertEquals("Message 3", messages.get(2).getContent())
        );
    }

    @Test
    @DisplayName("특정 메시지 이전의 메시지들을 페이징하여 조회한다")
    void successFindMessagesAfter() {
        // given
        saveMessagesInOrder(1L, 10);

        // when
        Slice<ChatMessage> messageSlice = chatMessageRepositoryImpl.findMessagesBefore(1L, 8L, 2);

        // then
        assertAll(
                () -> assertEquals(2, messageSlice.getContent().size(), "요청한 크기만큼 메시지가 조회되어야 합니다"),
                () -> assertEquals("Message 7", messageSlice.getContent().get(0).getContent()),
                () -> assertEquals("Message 6", messageSlice.getContent().get(1).getContent()),
                () -> assertTrue(messageSlice.hasNext(), "남은 메시지가 더 존재해야 합니다.")
        );
    }

    @Test
    @DisplayName("Enum 타입들이 올바르게 저장 및 조회된다")
    void successSaveAndFindEnumTypes() {
        // given
        chatMessageRepositoryImpl.save(chatMessage);

        // when
        List<ChatMessage> messages = chatMessageRepositoryImpl.findRecentMessages(1L, 1);
        ChatMessage foundMessage = messages.get(0);

        // then
        assertAll(
                () -> assertEquals(MessageContentType.TEXT, foundMessage.getContentType(),
                        "contentType이 올바르게 저장/조회되어야 합니다"),
                () -> assertEquals(MessageCategoryType.NORMAL, foundMessage.getCategoryType(),
                        "categoryType이 올바르게 저장/조회되어야 합니다")
        );
    }

    @Test
    @DisplayName("안 읽은 메시지 개수를 정확히 계산한다")
    void successCountUnreadMessages() {
        // given
        saveMessagesInOrder(1L, 5);

        // when
        Long unreadCount = chatMessageRepositoryImpl.countUnreadMessages(1L, 3L);

        // then
        assertEquals(2L, unreadCount, "마지막으로 읽은 메시지(ID: 3) 이후의 메시지 개수(4, 5)가 반환되어야 합니다");
    }

    @Test
    @DisplayName("메시지 내용이 5000자를 초과하면 저장 시 예외가 발생한다")
    void throwExceptionWhenContentExceeds5000Characters() {
        // given
        String longContent = "a".repeat(5001);

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> ChatMessageBuilder.builder()
                        .chatRoomId(1L)
                        .chatId(1L)
                        .content(longContent)
                        .contentType(MessageContentType.TEXT)
                        .categoryType(MessageCategoryType.NORMAL)
                        .sender(1L)
                        .build(),
                "메시지 내용이 5000자를 초과하면 예외가 발생해야 합니다");
    }

    @Test
    @DisplayName("BVA: 첫 페이지(가장 최근 메시지)부터 정상적으로 조회된다")
    void successFindFirstPage() {
        // given
        saveMessagesInOrder(1L, 5);

        // when
        Slice<ChatMessage> messageSlice = chatMessageRepositoryImpl.findMessagesBefore(1L, Long.MAX_VALUE, 2);

        // then
        assertAll(
                () -> assertEquals(2, messageSlice.getContent().size()),
                () -> assertEquals("Message 5", messageSlice.getContent().get(0).getContent()),
                () -> assertEquals("Message 4", messageSlice.getContent().get(1).getContent()),
                () -> assertTrue(messageSlice.hasNext())
        );
    }

    @Test
    @DisplayName("BVA: 마지막 페이지(가장 오래된 메시지)까지 정상적으로 조회된다")
    void successFindLastPage() {
        // given
        saveMessagesInOrder(1L, 5);

        // when
        Slice<ChatMessage> messageSlice = chatMessageRepositoryImpl.findMessagesBefore(1L, 2L, 2);

        // then
        assertAll(
                () -> assertEquals(1, messageSlice.getContent().size()),
                () -> assertEquals("Message 1", messageSlice.getContent().get(0).getContent()),
                () -> assertFalse(messageSlice.hasNext())
        );
    }

    @Test
    @DisplayName("여러 채팅방의 메시지가 서로 영향을 주지 않는다")
    void successMultipleRoomMessages() {
        // given
        saveMessagesInOrder(1L, 3);  // room 1
        saveMessagesInOrder(2L, 3);  // room 2

        // when
        List<ChatMessage> room1Messages = chatMessageRepositoryImpl.findRecentMessages(1L, 5);
        List<ChatMessage> room2Messages = chatMessageRepositoryImpl.findRecentMessages(2L, 5);

        // then
        assertAll(
                () -> assertEquals(3, room1Messages.size()),
                () -> assertEquals(3, room2Messages.size()),
                () -> assertTrue(room1Messages.stream().allMatch(msg -> msg.getChatRoomId().equals(1L))),
                () -> assertTrue(room2Messages.stream().allMatch(msg -> msg.getChatRoomId().equals(2L)))
        );
    }

    @Test
    @DisplayName("존재하지 않는 채팅방 조회 시 빈 목록을 반환한다")
    void returnEmptyForNonExistingRoom() {
        // when
        List<ChatMessage> messages = chatMessageRepositoryImpl.findRecentMessages(999L, 10);

        // then
        assertTrue(messages.isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 메시지 ID로 페이징 조회 시 빈 Slice를 반환한다")
    void returnEmptySliceForNonExistingMessage() {
        // when
        Slice<ChatMessage> messageSlice = chatMessageRepositoryImpl.findMessagesBefore(1L, 999L, 10);

        // then
        assertAll(
                () -> assertTrue(messageSlice.getContent().isEmpty()),
                () -> assertFalse(messageSlice.hasNext())
        );
    }

    @Test
    @DisplayName("요청한 크기가 전체 메시지 수보다 큰 경우에도 정상 동작한다")
    void successWithLargePageSize() {
        // given
        saveMessagesInOrder(1L, 3);

        // when
        List<ChatMessage> messages = chatMessageRepositoryImpl.findRecentMessages(1L, 10);

        // then
        assertEquals(3, messages.size());
    }

    @Test
    @DisplayName("동일한 시간에 생성된 메시지도 TSID 순서대로 정렬된다")
    void successSortingWithSameTimestamp() {
        // given
        int messageCount = 3;
        LocalDateTime now = LocalDateTime.now();
        for (long i = 1; i <= messageCount; i++) {
            ChatMessage message = ChatMessageBuilder.builder()
                    .chatRoomId(1L)
                    .chatId(i)
                    .content("Message " + i)
                    .contentType(MessageContentType.TEXT)
                    .categoryType(MessageCategoryType.NORMAL)
                    .sender(1L)
                    .build();
            ReflectionTestUtils.setField(message, "createdAt", now);

            chatMessageRepositoryImpl.save(message);
        }

        // when
        List<ChatMessage> messages = chatMessageRepositoryImpl.findRecentMessages(1L, 3);

        // then
        assertAll(
                () -> assertEquals(3, messages.size()),
                () -> assertEquals("Message 3", messages.get(0).getContent()),
                () -> assertEquals("Message 2", messages.get(1).getContent()),
                () -> assertEquals("Message 1", messages.get(2).getContent())
        );
    }

    private void saveMessagesInOrder(Long roomId, int messageCount) {
        for (long i = 1; i <= messageCount; i++) {
            ChatMessage message = ChatMessageBuilder.builder()
                    .chatRoomId(roomId)
                    .chatId(i)
                    .content("Message " + i)
                    .contentType(MessageContentType.TEXT)
                    .categoryType(MessageCategoryType.NORMAL)
                    .sender(1L)
                    .build();
            chatMessageRepositoryImpl.save(message);
        }
    }

    @AfterEach
    void tearDown() {
        Set<String> keys = redisTemplate.keys("chatroom:*:message");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
