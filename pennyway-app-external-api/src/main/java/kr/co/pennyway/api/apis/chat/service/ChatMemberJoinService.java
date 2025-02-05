package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.domain.common.annotation.DistributedLock;
import kr.co.pennyway.domain.context.account.service.UserService;
import kr.co.pennyway.domain.context.chat.service.ChatMemberService;
import kr.co.pennyway.domain.context.chat.service.ChatMessageService;
import kr.co.pennyway.domain.context.chat.service.ChatRoomService;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorCode;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorException;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.infra.common.event.ChatRoomJoinEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
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

    private final ChatMessageService chatMessageService;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 사용자가 채팅방에 참여하는 도메인 비즈니스 로직을 처리한다.
     * 채팅방 가입 가능 여부 확인을 위해 현재 가입한 회원 수를 조회하는데, 이 때 분산 락을 걸어 동시성 문제를 해결한다.
     *
     * @param userId     Long : 가입하려는 사용자의 ID
     * @param chatRoomId Long : 가입하려는 채팅방의 ID
     * @param password   Integer : 비공개 채팅방의 경우 비밀번호 정보를 입력받으며, 채팅방에 비밀번호가 없을 경우 null
     * @return Triple<ChatRoom, Integer, Long> : 가입한 채팅방 정보, 현재 채팅방의 회원 수, 읽지 않은 메시지 수
     */
    @DistributedLock(key = "'chat-room-join-' + #chatRoomId")
    public Triple<ChatRoom, Integer, Long> execute(Long userId, Long chatRoomId, Integer password) {
        ChatRoom chatRoom = chatRoomService.readChatRoom(chatRoomId).orElseThrow(() -> new ChatRoomErrorException(ChatRoomErrorCode.NOT_FOUND_CHAT_ROOM));

        if (chatMemberService.isExists(chatRoomId, userId)) {
            log.warn("이미 채팅방에 참여한 사용자입니다. chatRoomId: {}, userId: {}", chatRoomId, userId);
            throw new ChatRoomErrorException(ChatRoomErrorCode.AREADY_JOINED);
        }

        Long currentMemberCount = chatMemberService.countActiveMembers(chatRoomId);
        if (isFullRoom(currentMemberCount)) {
            log.warn("채팅방이 가득 찼습니다. chatRoomId: {}", chatRoomId);
            throw new ChatRoomErrorException(ChatRoomErrorCode.FULL_CHAT_ROOM);
        }

        if (chatRoom.isPrivateRoom() && !chatRoom.matchPassword(password)) {
            log.warn("채팅방 비밀번호가 일치하지 않습니다. chatRoomId: {}", chatRoomId);
            throw new ChatRoomErrorException(ChatRoomErrorCode.INVALID_PASSWORD);
        }

        User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));
        ChatMember member = chatMemberService.createMember(user, chatRoom);
        Long unreadMessageCount = chatMessageService.countUnreadMessages(chatRoomId, 0L);

        eventPublisher.publishEvent(ChatRoomJoinEvent.of(chatRoomId, user.getName()));

        return ImmutableTriple.of(chatRoom, currentMemberCount.intValue() + 1, unreadMessageCount);
    }

    private boolean isFullRoom(Long currentMemberCount) {
        return currentMemberCount >= MAX_MEMBER_COUNT;
    }
}
