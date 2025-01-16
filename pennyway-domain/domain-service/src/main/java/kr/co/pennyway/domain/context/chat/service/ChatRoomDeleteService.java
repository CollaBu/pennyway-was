package kr.co.pennyway.domain.context.chat.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.context.chat.dto.ChatRoomDeleteCommand;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomRdbService;
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
        var admin = chatMemberRdbService.readChatMember(command.userId(), command.chatRoomId())
                .orElseThrow(() -> new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND));

        if (!admin.isAdmin()) throw new ChatMemberErrorException(ChatMemberErrorCode.NOT_ADMIN);

        var chatRoom = admin.getChatRoom();

        chatMemberRdbService.deleteAllByChatRoomId(chatRoom.getId());
        chatRoomRdbService.delete(chatRoom);

        log.info("채팅방이 삭제되었습니다. chatRoom: {}", chatRoom);
    }
}
