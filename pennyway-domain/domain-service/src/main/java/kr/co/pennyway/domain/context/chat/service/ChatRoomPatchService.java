package kr.co.pennyway.domain.context.chat.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.context.chat.dto.ChatRoomPatchCommand;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorCode;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorException;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomRdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatRoomPatchService {
    private final ChatRoomRdbService chatRoomRdbService;
    
    @Transactional
    public ChatRoom execute(ChatRoomPatchCommand command) {
        ChatRoom chatRoom = chatRoomRdbService.readChatRoom(command.chatRoomId())
                .orElseThrow(() -> new ChatRoomErrorException(ChatRoomErrorCode.NOT_FOUND_CHAT_ROOM));

        chatRoom.update(command.title(), command.description(), command.backgroundImageUrl(), command.password());
        return chatRoomRdbService.update(chatRoom);
    }
}
