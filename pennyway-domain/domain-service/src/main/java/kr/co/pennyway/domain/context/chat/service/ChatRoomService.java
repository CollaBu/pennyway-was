package kr.co.pennyway.domain.context.chat.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomRdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRdbService chatRoomRdbService;

    @Transactional
    public ChatRoom create(ChatRoom chatRoom) {
        return chatRoomRdbService.create(chatRoom);
    }

    @Transactional(readOnly = true)
    public Optional<ChatRoom> readChatRoom(Long chatRoomId) {
        return chatRoomRdbService.readChatRoom(chatRoomId);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomDetail> readChatRoomsByUserId(Long userId) {
        return chatRoomRdbService.readChatRoomsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Slice<ChatRoomDetail> readChatRooms(Long userId, String target, Pageable pageable) {
        return chatRoomRdbService.readChatRooms(userId, target, pageable);
    }
}
