package kr.co.pennyway.domain.context.chat.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.context.chat.dto.ChatMemberBanCommand;
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
public class ChatMemberBanService {

    private final ChatMemberRdbService chatMemberRdbService;

    @Transactional
    public void execute(ChatMemberBanCommand command) {
        ChatMember admin = chatMemberRdbService.readChatMember(command.userId(), command.chatRoomId())
                .orElseThrow(() -> new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND));

        if (!admin.isAdmin()) {
            throw new ChatMemberErrorException(ChatMemberErrorCode.NOT_ADMIN);
        }

        ChatMember targetMember = chatMemberRdbService.readChatMemberByChatMemberId(command.targetMemberId())
                .orElseThrow(() -> new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND));

        targetMember.ban();

        chatMemberRdbService.update(targetMember);
    }
}
