package kr.co.pennyway.domain.domains.chatstatus.service;

import kr.co.pennyway.domain.config.ContainerDBTestConfig;
import kr.co.pennyway.domain.config.DomainIntegrationTest;
import kr.co.pennyway.domain.config.TestJpaConfig;
import kr.co.pennyway.domain.domains.chatstatus.domain.ChatMessageStatus;
import kr.co.pennyway.domain.domains.chatstatus.repository.ChatMessageStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DomainIntegrationTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestJpaConfig.class})
public class ChatMessageStatusServiceIntegrationTest extends ContainerDBTestConfig {
    @Autowired
    private ChatMessageStatusRepository chatMessageStatusRepository;

    @Autowired
    private ChatMessageStatusService chatMessageStatusService;

    @Test
    @DisplayName("메시지 읽음 상태를 정상적으로 저장하고 조회한다")
    void saveAndReadMessageStatus() {
        // given
        Long userId = 1L;
        Long chatRoomId = 1L;
        Long messageId = 100L;

        // when
        chatMessageStatusService.saveLastReadMessageId(userId, chatRoomId, messageId);
        Long lastReadId = chatMessageStatusService.readLastReadMessageId(userId, chatRoomId);

        // then
        assertEquals(messageId, lastReadId);
    }

    @Test
    @DisplayName("여러 메시지 읽음 상태를 벌크로 저장한다")
    void bulkSaveMessageStatus() {
        // given
        Map<Long, Map<Long, Long>> updates = new HashMap<>();

        // user1의 업데이트
        Map<Long, Long> user1Updates = new HashMap<>();
        user1Updates.put(1L, 100L);  // user1은 1번 방에서 100번 메시지까지 읽음
        user1Updates.put(2L, 200L);  // user1은 2번 방에서 200번 메시지까지 읽음
        updates.put(1L, user1Updates);

        // user2의 업데이트
        Map<Long, Long> user2Updates = new HashMap<>();
        user2Updates.put(1L, 150L);  // user2는 1번 방에서 150번 메시지까지 읽음
        updates.put(2L, user2Updates);

        // when
        chatMessageStatusService.bulkUpdateReadStatus(updates);

        // then
        assertAll(
                () -> assertEquals(100L, chatMessageStatusService.readLastReadMessageId(1L, 1L)),
                () -> assertEquals(200L, chatMessageStatusService.readLastReadMessageId(1L, 2L)),
                () -> assertEquals(150L, chatMessageStatusService.readLastReadMessageId(2L, 1L))
        );
    }

    @Test
    @DisplayName("더 작은 메시지 ID로 업데이트를 시도하면 무시된다")
    void ignoresSmallerMessageId() {
        // given
        Long userId = 1L;
        Long chatRoomId = 1L;

        // when
        chatMessageStatusService.saveLastReadMessageId(userId, chatRoomId, 100L);
        chatMessageStatusService.saveLastReadMessageId(userId, chatRoomId, 50L);

        // then
        assertEquals(100L, chatMessageStatusService.readLastReadMessageId(userId, chatRoomId));
    }

    @Test
    @DisplayName("존재하지 않는 읽음 상태 조회 시 0을 반환한다")
    void returnsZeroForNonExistentStatus() {
        // when
        Long result = chatMessageStatusService.readLastReadMessageId(999L, 999L);

        // then
        assertEquals(0L, result);
    }

    @Test
    @DisplayName("벌크 업데이트 시 기존 값보다 작은 메시지 ID는 업데이트되지 않는다")
    void bulkUpdateRespectsMessageIdOrder() {
        // given
        Long userId = 1L;
        Long chatRoomId = 1L;

        chatMessageStatusRepository.save(new ChatMessageStatus(userId, chatRoomId, 200L));

        Map<Long, Map<Long, Long>> updates = new HashMap<>();
        updates.put(userId, Map.of(chatRoomId, 100L)); // 1번 사용자가 1번 방에서 100번 메시지까지 읽음

        // when
        chatMessageStatusService.bulkUpdateReadStatus(updates);

        // then
        assertEquals(200L, chatMessageStatusService.readLastReadMessageId(userId, chatRoomId));
    }

    @AfterEach
    void tearDown() {
        chatMessageStatusRepository.deleteAll();
    }
}
