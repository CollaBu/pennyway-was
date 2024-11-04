package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.domain.common.redis.message.service.ChatMessageService;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomService;
import kr.co.pennyway.domain.domains.chatstatus.service.ChatMessageStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class ChatRoomSearchServiceTest {
    @InjectMocks
    private ChatRoomSearchService chatRoomSearchService;

    @Mock
    private ChatRoomService chatRoomService;
    @Mock
    private ChatMessageStatusService chatMessageStatusService;
    @Mock
    private ChatMessageService chatMessageService;

    @Test
    @DisplayName("사용자의 채팅방 목록과 각 방의 읽지 않은 메시지 수를 정상적으로 조회한다")
    void successReadChatRooms() {
        // given
        Long userId = 1L;
        List<ChatRoomDetail> chatRooms = List.of(
                new ChatRoomDetail(1L, "Room1", "", "", 123456, LocalDateTime.now(), true, 2),
                new ChatRoomDetail(2L, "Room2", "", "", null, LocalDateTime.now(), false, 2)
        );

        given(chatRoomService.readChatRoomsByUserId(userId)).willReturn(chatRooms);

        // room1: 마지막으로 읽은 메시지 ID 10, 읽지 않은 메시지 5개
        given(chatMessageStatusService.readLastReadMessageId(userId, 1L)).willReturn(10L);
        given(chatMessageService.countUnreadMessages(1L, 10L)).willReturn(5L);

        // room2: 마지막으로 읽은 메시지 ID 20, 읽지 않은 메시지 3개
        given(chatMessageStatusService.readLastReadMessageId(userId, 2L)).willReturn(20L);
        given(chatMessageService.countUnreadMessages(2L, 20L)).willReturn(3L);

        // when
        Map<ChatRoomDetail, Long> result = chatRoomSearchService.readChatRooms(userId);

        // then
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals(5L, result.get(chatRooms.get(0))),
                () -> assertEquals(3L, result.get(chatRooms.get(1)))
        );
    }

    @Test
    @DisplayName("채팅방이 없는 경우 빈 Map을 반환한다")
    void returnEmptyMapWhenNoRooms() {
        // given
        Long userId = 1L;
        given(chatRoomService.readChatRoomsByUserId(userId)).willReturn(Collections.emptyList());

        // when
        Map<ChatRoomDetail, Long> result = chatRoomSearchService.readChatRooms(userId);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("읽지 않은 메시지 수 조회 중, 모든 조회가 실패한다.")
    void continueProcessingOnError() {
        // given
        Long userId = 1L;
        List<ChatRoomDetail> chatRooms = List.of(
                new ChatRoomDetail(1L, "Room1", "", "", 123456, LocalDateTime.now(), true, 2),
                new ChatRoomDetail(2L, "Room2", "", "", null, LocalDateTime.now(), false, 2)
        );

        given(chatRoomService.readChatRoomsByUserId(userId)).willReturn(chatRooms);

        // room1: 정상 처리
        given(chatMessageStatusService.readLastReadMessageId(userId, 1L)).willReturn(10L);

        // room2: 오류 발생
        given(chatMessageStatusService.readLastReadMessageId(userId, 2L))
                .willThrow(new RuntimeException("Failed to get last read message id"));

        // when - then
        assertThrows(RuntimeException.class, () -> chatRoomSearchService.readChatRooms(userId));
    }

    @Test
    @DisplayName("각 서비스 호출이 정해진 순서대로 실행된다")
    void verifyServiceCallOrder() {
        // given
        Long userId = 1L;
        List<ChatRoomDetail> chatRooms = List.of(
                new ChatRoomDetail(1L, "Room1", "", "", 123456, LocalDateTime.now(), true, 2)
        );

        InOrder inOrder = inOrder(chatRoomService, chatMessageStatusService, chatMessageService);

        given(chatRoomService.readChatRoomsByUserId(userId)).willReturn(chatRooms);
        given(chatMessageStatusService.readLastReadMessageId(userId, 1L)).willReturn(10L);
        given(chatMessageService.countUnreadMessages(userId, 10L)).willReturn(5L);

        // when
        chatRoomSearchService.readChatRooms(userId);

        // then
        inOrder.verify(chatRoomService).readChatRoomsByUserId(userId);
        inOrder.verify(chatMessageStatusService).readLastReadMessageId(userId, 1L);
        inOrder.verify(chatMessageService).countUnreadMessages(userId, 10L);
    }
}
