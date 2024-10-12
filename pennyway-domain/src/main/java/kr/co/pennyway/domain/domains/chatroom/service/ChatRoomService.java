package kr.co.pennyway.domain.domains.chatroom.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public ChatRoom create(ChatRoom chatRoom) {
        return chatRoomRepository.save(chatRoom);
    }
}