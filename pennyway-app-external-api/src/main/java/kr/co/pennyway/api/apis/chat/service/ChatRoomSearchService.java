package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.api.apis.chat.dto.ChatRes;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.domain.context.chat.service.ChatMessageService;
import kr.co.pennyway.domain.context.chat.service.ChatMessageStatusService;
import kr.co.pennyway.domain.context.chat.service.ChatRoomService;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorCode;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorException;
import kr.co.pennyway.domain.domains.message.domain.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomSearchService {
    private final ChatRoomService chatRoomService;
    private final ChatMessageStatusService chatMessageStatusService;
    private final ChatMessageService chatMessageService;

    /**
     * 사용자 ID가 속한 채팅방 목록을 조회한다.
     *
     * @return 채팅방 목록. {@link ChatRoomRes.Info} 리스트 형태로 반환
     */
    @Transactional(readOnly = true)
    public List<ChatRoomRes.Info> readChatRooms(Long userId) {
        List<ChatRoomDetail> chatRooms = chatRoomService.readChatRoomsByUserId(userId);
        List<ChatRoomRes.Info> result = new ArrayList<>();

        for (ChatRoomDetail chatRoom : chatRooms) {
            Long lastReadMessageId = chatMessageStatusService.readLastReadMessageId(userId, chatRoom.id());
            ChatMessage lastMessage = chatMessageService.readRecentMessages(chatRoom.id(), 1).stream().findFirst().orElse(null);
            Long unreadCount = chatMessageService.countUnreadMessages(chatRoom.id(), lastReadMessageId);

            result.add(ChatRoomRes.Info.of(chatRoom, unreadCount, lastMessage == null ? null : ChatRes.ChatDetail.from(lastMessage)));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public ChatRoom readChatRoom(Long chatRoomId) {
        return chatRoomService.readChatRoom(chatRoomId).orElseThrow(() -> new ChatRoomErrorException(ChatRoomErrorCode.NOT_FOUND_CHAT_ROOM));
    }

    @Transactional(readOnly = true)
    public Slice<ChatRoomDetail> readChatRoomsBySearch(Long userId, String target, Pageable pageable) {
        return chatRoomService.readChatRooms(userId, target, pageable);
    }
}
