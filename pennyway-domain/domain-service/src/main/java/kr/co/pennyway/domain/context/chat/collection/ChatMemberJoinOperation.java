package kr.co.pennyway.domain.context.chat.collection;

import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorCode;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorException;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

@Slf4j
public class ChatMemberJoinOperation {
    private static final long MAX_MEMBER_COUNT = 300;

    private final User user;
    private final ChatRoom chatRoom;
    private final Long currentMemberCount;

    public ChatMemberJoinOperation(@NonNull User user, @NonNull ChatRoom chatRoom, @NonNull Long currentMemberCount,) {
        this.user = user;
        this.chatRoom = chatRoom;
        this.currentMemberCount = currentMemberCount;
    }

    /**
     * 사용자가 채팅방에 참여하는 도메인 비즈니스 로직을 처리한다.
     *
     * <pre>
     * [비즈니스 규칙]
     * - 채팅방에 이미 참여한 사용자는 다시 참여할 수 없다.
     * - 채팅방이 가득 찼을 경우, 채팅방에 참여할 수 없다.
     * - 비밀번호가 일치하지 않는 경우, 채팅방에 참여할 수 없다.
     * - 참여한 사용자는 일반 멤버 권한을 부여받는다.
     * </pre>
     *
     * @return ChatMember : 채팅방에 참여한 사용자 정보
     * @throws ChatMemberErrorException {@link ChatMemberErrorCode#ALREADY_JOINED} : 이미 채팅방에 참여한 사용자가 다시 참여하는 경우
     * @throws ChatRoomErrorException   {@link ChatRoomErrorCode#FULL_CHAT_ROOM} : 채팅방이 가득 찬 경우
     * @throws ChatRoomErrorException   {@link ChatRoomErrorCode#INVALID_PASSWORD} : 비밀번호가 일치하지 않는 경우
     */
    public ChatMember execute(Integer password) {
        if (isAlreadyJoined()) {
            // 이 규칙은 현재 ChatMemberRdbService의 createMember 메서드에서 처리하고 있습니다.
            // 도메인 규칙의 누수 여부를 검증하기 전까지는 이 규칙을 구현하지 않습니다.
        }

        if (isFullRoom()) {
            log.warn("채팅방이 가득 찼습니다. chatRoomId: {}", chatRoom.getId());
            throw new ChatRoomErrorException(ChatRoomErrorCode.FULL_CHAT_ROOM);
        }

        if (matchPassword(password)) {
            log.warn("채팅방 비밀번호가 일치하지 않습니다. chatRoomId: {}", chatRoom.getId());
            throw new ChatRoomErrorException(ChatRoomErrorCode.INVALID_PASSWORD);
        }

        return ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER);
    }

    private boolean isAlreadyJoined() {
        return false;
    }

    private boolean isFullRoom() {
        return currentMemberCount >= MAX_MEMBER_COUNT;
    }

    private boolean matchPassword(Integer password) {
        return chatRoom.isPrivateRoom() && chatRoom.matchPassword(password);
    }
}
