package kr.co.pennyway.domain.context.chat.service;

import kr.co.pennyway.common.annotation.DomainService;
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

    @Transactional
    public void execute(Long chatMemberId) {
        ChatMember chatMember = chatMemberRdbService.readChatMemberByChatMemberId(chatMemberId)
                .orElseThrow(() -> {
                    log.warn("채팅방 멤버를 찾을 수 없습니다. chatMemberId: {}", chatMemberId);
                    return new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND);
                });

        chatMember.leave();
        chatMemberRdbService.update(chatMember);
    }
}
