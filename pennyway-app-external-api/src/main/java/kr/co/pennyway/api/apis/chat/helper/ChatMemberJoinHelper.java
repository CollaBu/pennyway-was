package kr.co.pennyway.api.apis.chat.helper;

import kr.co.pennyway.common.annotation.Helper;
import kr.co.pennyway.domain.context.chat.dto.ChatMemberJoinCommand;
import kr.co.pennyway.domain.context.chat.service.ChatMemberJoinService;
import kr.co.pennyway.domain.context.chat.service.ChatMessageService;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.infra.common.event.ChatRoomJoinEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@Helper
@RequiredArgsConstructor
public class ChatMemberJoinHelper {
    private final ChatMemberJoinService chatMemberJoinService;
    private final ChatMessageService chatMessageService;

    private final ApplicationEventPublisher eventPublisher;

    public Triple<ChatRoom, Integer, Long> execute(Long userId, Long chatRoomId, Integer password) {
        var chatMemberJoinResult = chatMemberJoinService.execute(ChatMemberJoinCommand.of(userId, chatRoomId, password));

        Long unreadMessageCount = chatMessageService.countUnreadMessages(chatRoomId, 0L);

        eventPublisher.publishEvent(ChatRoomJoinEvent.of(chatRoomId, chatMemberJoinResult.memberName()));

        return ImmutableTriple.of(chatMemberJoinResult.chatRoom(), chatMemberJoinResult.currentMemberCount().intValue(), unreadMessageCount);
    }
}
