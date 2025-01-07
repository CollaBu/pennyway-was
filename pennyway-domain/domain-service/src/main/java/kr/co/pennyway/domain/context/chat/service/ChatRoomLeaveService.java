package kr.co.pennyway.domain.context.chat.service;

import kr.co.pennyway.common.annotation.DomainService;
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
public class ChatRoomLeaveService {
    private final ChatMemberRdbService chatMemberRdbService;
    private final ChatRoomRdbService chatRoomRdbService;

    @Transactional
    public void execute(Long chatMemberId) {
        ChatMember chatMember = chatMemberRdbService.readChatMemberByChatMemberId(chatMemberId)
                .orElseThrow(() -> {
                    log.warn("채팅방 멤버를 찾을 수 없습니다. chatMemberId: {}", chatMemberId);
                    return new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND);
                });

        // 방장이면 채팅방에 자신만 남아있어야 하고, 그렇지 않으면 예외를 던진다.
        // 만약 삭제 가능하다면, 방장을 탈퇴시키고 채팅방을 삭제한다.
        if (chatMember.isAdmin()) {
            ChatRoom chatRoom = chatMember.getChatRoom();

            if (chatRoom.hasOnlyAdmin()) {
                chatMember.leave();
                chatMemberRdbService.update(chatMember);
                chatRoomRdbService.delete(chatRoom);
            } else {
                log.warn("채팅방 방장은 채팅방을 탈퇴할 수 없습니다. chatRoomId: {}, chatMemberId: {}", chatRoom.getId(), chatMemberId);
                throw new ChatMemberErrorException(ChatMemberErrorCode.ADMIN_CANNOT_LEAVE);
            }
        }

        chatMember.leave();
        chatMemberRdbService.update(chatMember);
    }
}
