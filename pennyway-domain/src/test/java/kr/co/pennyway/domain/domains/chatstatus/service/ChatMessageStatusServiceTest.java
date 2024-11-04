package kr.co.pennyway.domain.domains.chatstatus.service;

import kr.co.pennyway.domain.domains.chatstatus.domain.ChatMessageStatus;
import kr.co.pennyway.domain.domains.chatstatus.repository.ChatMessageStatusRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class ChatMessageStatusServiceTest {
    @InjectMocks
    private ChatMessageStatusService chatMessageStatusService;

    @Mock
    private ChatMessageStatusRepository chatMessageStatusRepository;

    private static Stream<Arguments> provideInvalidInputs() {
        return Stream.of(
                Arguments.of(null, 1L, 1L),
                Arguments.of(1L, null, 1L),
                Arguments.of(1L, 1L, null),
                Arguments.of(-1L, 1L, 1L),
                Arguments.of(1L, -1L, 1L),
                Arguments.of(1L, 1L, -1L)
        );
    }

    @Test
    @DisplayName("캐시에서 마지막 읽은 메시지 ID를 조회한다")
    void getLastReadMessageIdFromCache() {
        // given
        Long userId = 1L;
        Long chatRoomId = 1L;
        Long messageId = 100L;

        given(chatMessageStatusRepository.findLastReadMessageId(userId, chatRoomId)).willReturn(Optional.of(messageId));

        // when
        Long result = chatMessageStatusService.readLastReadMessageId(userId, chatRoomId);

        // then
        assertEquals(messageId, result);
        verify(chatMessageStatusRepository).findLastReadMessageId(userId, chatRoomId);
        verifyNoMoreInteractions(chatMessageStatusRepository);
    }

    @Test
    @DisplayName("캐시 미스 시 DB에서 조회하고 캐시를 갱신한다")
    void getLastReadMessageIdFromDB() {
        // given
        Long userId = 1L;
        Long chatRoomId = 1L;
        Long messageId = 100L;

        ChatMessageStatus status = new ChatMessageStatus(userId, chatRoomId, messageId);

        given(chatMessageStatusRepository.findLastReadMessageId(userId, chatRoomId)).willReturn(Optional.empty());
        given(chatMessageStatusRepository.findByUserIdAndChatRoomId(userId, chatRoomId)).willReturn(Optional.of(status));

        // when
        Long result = chatMessageStatusService.readLastReadMessageId(userId, chatRoomId);

        // then
        assertEquals(messageId, result);
        verify(chatMessageStatusRepository).findLastReadMessageId(userId, chatRoomId);
        verify(chatMessageStatusRepository).findByUserIdAndChatRoomId(userId, chatRoomId);
        verify(chatMessageStatusRepository).saveLastReadMessageId(userId, chatRoomId, messageId);
    }

    @Test
    @DisplayName("DB에도 데이터가 없는 경우 0을 반환한다")
    void returnZeroWhenNoDataExists() {
        // given
        Long userId = 1L;
        Long chatRoomId = 1L;

        given(chatMessageStatusRepository.findLastReadMessageId(userId, chatRoomId))
                .willReturn(Optional.empty());
        given(chatMessageStatusRepository.findByUserIdAndChatRoomId(userId, chatRoomId))
                .willReturn(Optional.empty());

        // when
        Long result = chatMessageStatusService.readLastReadMessageId(userId, chatRoomId);

        // then
        assertEquals(0L, result);
        verify(chatMessageStatusRepository).findLastReadMessageId(userId, chatRoomId);
        verify(chatMessageStatusRepository).findByUserIdAndChatRoomId(userId, chatRoomId);
        verifyNoMoreInteractions(chatMessageStatusRepository);
    }

    @Test
    @DisplayName("벌크 업데이트 시 모든 항목이 정상적으로 처리된다")
    void bulkUpdateProcessesAllItems() {
        // given
        Map<Long, Map<Long, Long>> updates = new HashMap<>();
        Map<Long, Long> user1Updates = new HashMap<>();
        user1Updates.put(1L, 100L);
        user1Updates.put(2L, 200L);

        Map<Long, Long> user2Updates = new HashMap<>();
        user2Updates.put(1L, 150L);

        updates.put(1L, user1Updates);
        updates.put(2L, user2Updates);

        // when
        chatMessageStatusService.bulkUpdateReadStatus(updates);

        // then
        verify(chatMessageStatusRepository).saveLastReadMessageIdInBulk(1L, 1L, 100L);
        verify(chatMessageStatusRepository).saveLastReadMessageIdInBulk(1L, 2L, 200L);
        verify(chatMessageStatusRepository).saveLastReadMessageIdInBulk(2L, 1L, 150L);
        verify(chatMessageStatusRepository).deleteLastReadMessageId(1L, 1L);
        verify(chatMessageStatusRepository).deleteLastReadMessageId(1L, 2L);
        verify(chatMessageStatusRepository).deleteLastReadMessageId(2L, 1L);
    }

    @Test
    @DisplayName("새 메시지 저장 시 정상적으로 처리된다")
    void saveNewMessageStatus() {
        // given
        Long userId = 1L;
        Long chatRoomId = 1L;
        Long messageId = 100L;

        // when
        chatMessageStatusService.saveLastReadMessageId(userId, chatRoomId, messageId);

        // then
        verify(chatMessageStatusRepository).saveLastReadMessageId(userId, chatRoomId, messageId);
        verifyNoMoreInteractions(chatMessageStatusRepository);
    }

    @Test
    @DisplayName("cache repository에서 예외 발생 시 적절히 처리된다 (현재는 예외를 던짐)")
    void handleRepositoryException() {
        // given
        Long userId = 1L;
        Long chatRoomId = 1L;

        given(chatMessageStatusRepository.findLastReadMessageId(userId, chatRoomId)).willThrow(new RuntimeException("Redis connection failed"));

        // when - then
        assertThrows(RuntimeException.class, () ->
                chatMessageStatusService.readLastReadMessageId(userId, chatRoomId)
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidInputs")
    @DisplayName("잘못된 입력값이 들어올 경우 적절히 처리된다")
    void handleInvalidInputs(Long userId, Long chatRoomId, Long messageId) {
        assertThrows(IllegalArgumentException.class, () ->
                chatMessageStatusService.saveLastReadMessageId(userId, chatRoomId, messageId)
        );
    }
}
