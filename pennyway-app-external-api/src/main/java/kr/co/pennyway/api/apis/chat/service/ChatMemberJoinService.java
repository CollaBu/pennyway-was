package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.domain.common.redisson.DistributedLock;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorCode;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorException;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomService;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.service.ChatMemberService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.infra.common.event.ChatRoomJoinEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMemberJoinService {
    private static final long MAX_MEMBER_COUNT = 300;

    private final UserService userService;
    private final ChatRoomService chatRoomService;
    private final ChatMemberService chatMemberService;

    private final ApplicationEventPublisher eventPublisher;

    @DistributedLock(key = "chatRoom.join.#chatRoomId")
    public void execute(Long userId, Long chatRoomId, Integer password) {
        ChatRoom chatRoom = chatRoomService.readChatRoom(chatRoomId).orElseThrow(() -> new ChatRoomErrorException(ChatRoomErrorCode.NOT_FOUND_CHAT_ROOM));

        if (isFullRoom(chatRoomId)) {
            throw new ChatRoomErrorException(ChatRoomErrorCode.FULL_CHAT_ROOM);
        }

        if (chatRoom.isPrivateRoom() && !chatRoom.matchPassword(password)) {
            throw new ChatRoomErrorException(ChatRoomErrorCode.INVALID_PASSWORD);
        }

        User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));
        ChatMember member = chatMemberService.createMember(user, chatRoom);

        eventPublisher.publishEvent(ChatRoomJoinEvent.of(chatRoomId, member.getName()));
    }

    private boolean isFullRoom(Long chatRoomId) {
        return chatMemberService.countActiveMembers(chatRoomId) >= MAX_MEMBER_COUNT;
    }
}
