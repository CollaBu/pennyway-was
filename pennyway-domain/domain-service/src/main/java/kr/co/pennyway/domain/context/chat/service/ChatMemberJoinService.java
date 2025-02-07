package kr.co.pennyway.domain.context.chat.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.common.annotation.DistributedLock;
import kr.co.pennyway.domain.context.chat.collection.ChatMemberJoinOperation;
import kr.co.pennyway.domain.context.chat.dto.ChatMemberJoinCommand;
import kr.co.pennyway.domain.context.chat.dto.ChatMemberJoinResult;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorCode;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorException;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomRdbService;
import kr.co.pennyway.domain.domains.member.service.ChatMemberRdbService;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserRdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMemberJoinService {
    private final UserRdbService userRdbService;
    private final ChatRoomRdbService chatRoomRdbService;
    private final ChatMemberRdbService chatMemberRdbService;

    @DistributedLock(key = "'chat-room-join-' + #command.chatRoomId()", leaseTime = 10L)
    public ChatMemberJoinResult execute(ChatMemberJoinCommand command) {
        var user = userRdbService.readUser(command.userId()).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));
        var chatRoom = chatRoomRdbService.readChatRoom(command.chatRoomId()).orElseThrow(() -> new ChatRoomErrorException(ChatRoomErrorCode.NOT_FOUND_CHAT_ROOM));
        var chatMembers = chatMemberRdbService.readChatMembersByChatRoomId(command.chatRoomId())
                .stream().filter(chatMember -> chatMember.isActive() && !chatMember.isBanned())
                .collect(Collectors.toSet());

        var newChatMember = new ChatMemberJoinOperation(user, chatRoom, chatMembers)
                .execute(command.password());
        chatMemberRdbService.create(newChatMember);

        return ChatMemberJoinResult.of(chatRoom, user.getName(), (long) chatMembers.size() + 1);
    }
}
