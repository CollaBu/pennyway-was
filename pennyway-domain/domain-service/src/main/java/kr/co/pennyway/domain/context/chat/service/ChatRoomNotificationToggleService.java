package kr.co.pennyway.domain.context.chat.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.context.chat.dto.ChatRoomToggleCommand;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import kr.co.pennyway.domain.domains.member.service.ChatMemberRdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatRoomNotificationToggleService {
    private final ChatMemberRdbService chatMemberRdbService;

    @Transactional
    public void turnOn(ChatRoomToggleCommand command) {
        var chatMember = chatMemberRdbService.readChatMember(command.userId(), command.chatRoomId())
                .orElseThrow(() -> new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND));

        chatMember.enableNotify();

        log.info("{}님이 {} 채팅방의 알림을 켰습니다.", chatMember.getId(), command.chatRoomId());
    }

    @Transactional
    public void turnOff(ChatRoomToggleCommand command) {
        var chatMember = chatMemberRdbService.readChatMember(command.userId(), command.chatRoomId())
                .orElseThrow(() -> new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND));

        chatMember.disableNotify();
        
        log.info("{}님이 {} 채팅방의 알림을 껐습니다.", chatMember.getId(), command.chatRoomId());
    }
}
