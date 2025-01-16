package kr.co.pennyway.domain.context.chat.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.context.chat.dto.ChatRoomDeleteCommand;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomRdbService;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import kr.co.pennyway.domain.domains.member.service.ChatMemberRdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatRoomDeleteService {
    private final ChatMemberRdbService chatMemberRdbService;
    private final ChatRoomRdbService chatRoomRdbService;

    @Transactional
    public void deleteChatRoom(ChatRoomDeleteCommand command) {
        ChatMember admin = chatMemberRdbService.readChatMember(command.userId(), command.chatRoomId())
                .orElseThrow(() -> new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND));

        if (!admin.isAdmin()) throw new ChatMemberErrorException(ChatMemberErrorCode.NOT_ADMIN);

        ChatRoom chatRoom = admin.getChatRoom();

        chatMemberRdbService.deleteAllByChatRoomId(chatRoom.getId());
        chatRoomRdbService.delete(chatRoom);
    }
}
