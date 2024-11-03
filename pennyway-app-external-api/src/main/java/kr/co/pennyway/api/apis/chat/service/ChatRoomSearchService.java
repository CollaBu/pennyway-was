package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.domain.common.redis.message.service.ChatMessageService;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomService;
import kr.co.pennyway.domain.domains.chatstatus.service.ChatMessageStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

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
     * @return 채팅방 목록 (채팅방 정보, 읽지 않은 메시지 수)
     */
    @Transactional(readOnly = true)
    public Map<ChatRoomDetail, Long> readChatRooms(Long userId) {
        return chatRoomService.readChatRoomsByUserId(userId).stream()
                .collect(Collectors.toMap(chatRoom -> chatRoom, chatRoom -> chatMessageStatusService.readLastReadMessageId(userId, chatRoom.id())))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> chatMessageService.countUnreadMessages(userId, entry.getValue())));
    }

    @Transactional(readOnly = true)
    public Slice<ChatRoomDetail> readChatRoomsBySearch(Long userId, String target, Pageable pageable) {
        return chatRoomService.readChatRooms(userId, target, pageable);
    }
}
