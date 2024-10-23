package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomSearchService {
    private final ChatRoomService chatRoomService;

    @Transactional(readOnly = true)
    public List<ChatRoomDetail> readChatRooms(Long userId) {
        return chatRoomService.readChatRoomsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Slice<ChatRoomDetail> readChatRoomsBySearch(Long userId, String target, Pageable pageable) {
        return chatRoomService.readChatRooms(userId, target, pageable);
    }
}
