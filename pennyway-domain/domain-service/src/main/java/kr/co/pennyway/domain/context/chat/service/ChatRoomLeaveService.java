package kr.co.pennyway.domain.context.chat.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.context.chat.collection.ChatRoomLeaveCollection;
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
    public void execute(Long userId, Long chatRoomId) {
        ChatMember chatMember = chatMemberRdbService.readChatMember(userId, chatRoomId)
                .orElseThrow(() -> {
                    log.warn("{}번 방에서 {}번 사용자의 채팅방 멤버 정보를를 찾을 수 없습니다.", chatRoomId, userId);
                    return new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND);
                });

        var result = new ChatRoomLeaveCollection(chatMember).leave();

        chatMemberRdbService.update(result.chatMember());
        if (result.shouldDeleteChatRoom()) {
            chatRoomRdbService.delete(chatMember.getChatRoom());
        }
    }
}
