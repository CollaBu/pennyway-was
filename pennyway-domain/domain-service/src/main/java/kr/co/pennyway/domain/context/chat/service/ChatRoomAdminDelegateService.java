package kr.co.pennyway.domain.context.chat.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.context.chat.collection.ChatRoomAdminDelegateOperation;
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
    public void execute(Long chatRoomId, Long chatAdminUserId, Long targetChatMemberId) {
        var chatAdmin = chatMemberRdbService.readChatMember(chatAdminUserId, chatRoomId)
                .orElseThrow(() -> new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND));
        var targetMember = chatMemberRdbService.readChatMemberByChatMemberId(targetChatMemberId)
                .orElseThrow(() -> new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND));

        new ChatRoomAdminDelegateOperation(chatAdmin, targetMember).execute();
    }
}
