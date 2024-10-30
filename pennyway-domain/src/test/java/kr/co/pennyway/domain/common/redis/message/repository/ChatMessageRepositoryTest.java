package kr.co.pennyway.domain.common.redis.message.repository;

import kr.co.pennyway.domain.common.redis.message.domain.ChatMessage;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessageBuilder;
import kr.co.pennyway.domain.common.redis.message.type.MessageCategoryType;
import kr.co.pennyway.domain.common.redis.message.type.MessageContentType;
import kr.co.pennyway.domain.config.ContainerRedisTestConfig;
import kr.co.pennyway.domain.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ContextConfiguration(classes = {RedisConfig.class})
@DataRedisTest(properties = "spring.config.location=classpath:application-domain.yml")
@ActiveProfiles("test")
public class ChatMessageRepositoryTest extends ContainerRedisTestConfig {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private ChatMessage chatMessage;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Happy Path: 채팅 메시지 저장에 성공한다.")
    void successSaveChatMessage() {
        // when
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // then
        log.info("Saved message: {}", savedMessage);
        assertAll(
                () -> assertNotNull(savedMessage, "저장된 메시지는 null이 아니어야 합니다"),
                () -> assertTrue(savedMessage.getId().matches("chatroom:\\d+:message:\\d+"), "ID는 'chatroom:{roomId}:message:{messageId}' 형태여야 합니다"),
                () -> assertEquals(chatMessage.getId(), savedMessage.getId(), "저장 전후의 ID가 동일해야 합니다")
        );
    }

    @Test
    @DisplayName("ID로 채팅 메시지 조회에 성공한다.")
    void successFindChatMessageById() {
        // given
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // when
        Optional<ChatMessage> foundMessage = chatMessageRepository.findById(chatMessage.getId());

        // then
        assertAll(
                () -> assertTrue(foundMessage.isPresent(), "저장된 메시지는 조회할 수 있어야 합니다"),
                () -> assertEquals(savedMessage.getId(), foundMessage.get().getId(), "조회된 메시지의 ID가 일치해야 합니다")
        );
    }

    @Test
    @DisplayName("Enum 타입들이 올바르게 저장 및 조회된다.")
    void successSaveAndFindEnumTypes() {
        // given
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // when
        Optional<ChatMessage> foundMessage = chatMessageRepository.findById(savedMessage.getId());

        // then
        assertAll(
                () -> assertTrue(foundMessage.isPresent(), "저장된 메시지는 조회할 수 있어야 합니다"),
                () -> assertEquals(MessageContentType.TEXT, foundMessage.get().getContentType(),
                        "contentType이 올바르게 저장/조회되어야 합니다"),
                () -> assertEquals(MessageCategoryType.NORMAL, foundMessage.get().getCategoryType(),
                        "categoryType이 올바르게 저장/조회되어야 합니다")
        );
    }

    @Test
    @DisplayName("동일한 메시지를 여러 스레드에서 동시에 저장할 때, 중복 저장되지 않는다.")
    void successSaveChatMessageConcurrently() throws InterruptedException {
        // given
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        Set<String> savedIds = ConcurrentHashMap.newKeySet();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    ChatMessage saved = chatMessageRepository.save(chatMessage);
                    savedIds.add(saved.getId());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        assertEquals(1, savedIds.size(), "동일한 ID의 메시지는 한 번만 저장되어야 합니다");
        ChatMessage foundMessage = chatMessageRepository.findById(chatMessage.getId()).orElse(null);
        assertNotNull(foundMessage, "저장된 메시지는 조회할 수 있어야 합니다");
    }

    @Test
    @DisplayName("데이터 정합성 검증")
    void successCheckDataIntegrity() {
        // given
        chatMessageRepository.save(chatMessage);

        // when
        ChatMessage foundMessage = chatMessageRepository.findById(chatMessage.getId()).orElse(null);

        // then
        log.info("Found message: {}", foundMessage);
        assertAll(
                () -> assertEquals(chatMessage.getId(), foundMessage.getId()),
                () -> assertEquals(chatMessage.getContent(), foundMessage.getContent()),
                () -> assertEquals(chatMessage.getContentType(), foundMessage.getContentType()),
                () -> assertEquals(chatMessage.getCategoryType(), foundMessage.getCategoryType()),
                () -> assertEquals(chatMessage.getCreatedAt(), foundMessage.getCreatedAt()),
                () -> assertEquals(chatMessage.getDeletedAt(), foundMessage.getDeletedAt()),
                () -> assertEquals(chatMessage.getSender(), foundMessage.getSender())
        );
    }

    @Test
    @DisplayName("메시지 내용이 5000자를 초과하면 저장 시 예외가 발생한다.")
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
}
