package kr.co.pennyway.domain.context.chat.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.common.annotation.DistributedLock;
import kr.co.pennyway.domain.context.chat.collection.ChatRoomAdminDelegateOperation;
import kr.co.pennyway.domain.context.chat.dto.ChatRoomAdminDelegateCommand;
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
public class ChatRoomAdminDelegateService {
    private final ChatMemberRdbService chatMemberRdbService;

    @Transactional
    @DistributedLock(key = "'chat-room-admin-delegate-' + #command.chatRoomId()")
    public void execute(ChatRoomAdminDelegateCommand command) {
        var chatAdmin = chatMemberRdbService.readChatMember(command.chatAdminUserId(), command.chatRoomId())
                .filter(ChatMember::isActive)
                .orElseThrow(() -> new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND));

        var targetMember = chatMemberRdbService.readChatMemberByChatMemberId(command.targetChatMemberId())
                .filter(ChatMember::isActive)
                .orElseThrow(() -> new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND));

        new ChatRoomAdminDelegateOperation(chatAdmin, targetMember).execute();
    }
}
