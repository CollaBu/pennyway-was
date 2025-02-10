package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.domain.context.chat.service.ChatMessageService;
import kr.co.pennyway.domain.context.chat.service.ChatMessageStatusService;
import kr.co.pennyway.domain.context.chat.service.ChatRoomService;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import kr.co.pennyway.domain.domains.message.domain.ChatMessage;
import kr.co.pennyway.domain.domains.message.domain.ChatMessageBuilder;
import kr.co.pennyway.domain.domains.message.type.MessageCategoryType;
import kr.co.pennyway.domain.domains.message.type.MessageContentType;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
    @DisplayName("사용자의 채팅방 목록과 각 방의 읽지 않은 메시지 수, 마지막 메시지를 정상적으로 조회한다")
    void successReadChatRooms() {
        // given
        Long userId = 1L;
        List<ChatRoomDetail> chatRooms = List.of(
                new ChatRoomDetail(1L, "Room1", "", "", 123456, LocalDateTime.now(), true, 2, true),
                new ChatRoomDetail(2L, "Room2", "", "", null, LocalDateTime.now(), false, 2, true)
        );

        given(chatRoomService.readChatRoomsByUserId(userId)).willReturn(chatRooms);

        // room1: 마지막으로 읽은 메시지 ID 10, 읽지 않은 메시지 5개
        ChatMessage firstRoomLastMessage = ChatMessageBuilder.builder().chatRoomId(2L).chatId(1L).content("Hello").contentType(MessageContentType.TEXT).categoryType(MessageCategoryType.NORMAL).sender(userId).build();

        given(chatMessageStatusService.readLastReadMessageId(userId, 1L)).willReturn(10L);
        given(chatMessageService.countUnreadMessages(1L, 10L)).willReturn(5L);
        given(chatMessageService.readRecentMessages(1L, 1)).willReturn(List.of(firstRoomLastMessage));

        // room2: 마지막으로 읽은 메시지 ID 20, 읽지 않은 메시지 3개
        ChatMessage secondRoomLastMessage = ChatMessageBuilder.builder().chatRoomId(2L).chatId(100L).content("jayang님이 입장하셨습니다.").contentType(MessageContentType.TEXT).categoryType(MessageCategoryType.SYSTEM).sender(userId).build();

        given(chatMessageStatusService.readLastReadMessageId(userId, 2L)).willReturn(20L);
        given(chatMessageService.countUnreadMessages(2L, 20L)).willReturn(3L);
        given(chatMessageService.readRecentMessages(2L, 1)).willReturn(List.of(secondRoomLastMessage));

        // when
        List<ChatRoomRes.Info> result = chatRoomSearchService.readChatRooms(userId);

        // then
        assertAll(
                () -> assertEquals(2, result.size(), "조회된 채팅방 목록은 2개여야 한다."),
                () -> assertEquals(5L, result.get(0).unreadMessageCount(), "Room1의 읽지 않은 메시지 수는 5개여야 한다."),
                () -> assertEquals(firstRoomLastMessage.getChatId(), result.get(0).lastMessage().chatId(), "Room1의 마지막 메시지는 ID가 일치해야 한다."),
                () -> assertEquals(3L, result.get(1).unreadMessageCount(), "Room2의 읽지 않은 메시지 수는 3개여야 한다."),
                () -> assertEquals(secondRoomLastMessage.getChatId(), result.get(1).lastMessage().chatId(), "Room2의 마지막 메시지는 ID가 일치해야 한다.")
        );
    }

    @Test
    @DisplayName("채팅방이 없는 경우 빈 Map을 반환한다")
    void returnEmptyMapWhenNoRooms() {
        // given
        Long userId = 1L;
        given(chatRoomService.readChatRoomsByUserId(userId)).willReturn(Collections.emptyList());

        // when
        List<ChatRoomRes.Info> result = chatRoomSearchService.readChatRooms(userId);

        // then
        assertTrue(result.isEmpty());
        verify(chatMessageStatusService, never()).readLastReadMessageId(eq(userId), anyLong());
        verify(chatMessageService, never()).countUnreadMessages(anyLong(), anyLong());
        verify(chatMessageService, never()).countUnreadMessages(anyLong(), anyLong());
    }

    @Test
    @DisplayName("읽지 않은 메시지 수 조회 중, 모든 조회가 실패한다.")
    void continueProcessingOnError() {
        // given
        Long userId = 1L;
        List<ChatRoomDetail> chatRooms = List.of(
                new ChatRoomDetail(1L, "Room1", "", "", 123456, LocalDateTime.now(), true, 2, true),
                new ChatRoomDetail(2L, "Room2", "", "", null, LocalDateTime.now(), false, 2, true)
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
                new ChatRoomDetail(1L, "Room1", "", "", 123456, LocalDateTime.now(), true, 2, true)
        );

        InOrder inOrder = inOrder(chatRoomService, chatMessageStatusService, chatMessageService, chatMessageService);

        given(chatRoomService.readChatRoomsByUserId(userId)).willReturn(chatRooms);
        given(chatMessageStatusService.readLastReadMessageId(userId, 1L)).willReturn(10L);
        given(chatMessageService.readRecentMessages(1L, 1)).willReturn(Collections.emptyList());
        given(chatMessageService.countUnreadMessages(userId, 10L)).willReturn(5L);

        // when
        chatRoomSearchService.readChatRooms(userId);

        // then
        inOrder.verify(chatRoomService).readChatRoomsByUserId(userId);
        inOrder.verify(chatMessageStatusService).readLastReadMessageId(userId, 1L);
        inOrder.verify(chatMessageService).countUnreadMessages(userId, 10L);
    }
}
