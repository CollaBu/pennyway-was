package kr.co.pennyway.domain.context.chat.collection;

import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatRoomLeaveCollection {
    private final ChatMember chatMember;

    public ChatRoomLeaveCollection(ChatMember chatMember) {
        this.chatMember = chatMember;
    }

    /**
     * 채팅방 멤버의 퇴장을 처리합니다.
     *
     * <pre>
     * [비즈니스 규칙]
     * - 방장(admin)과 일반 멤버는 서로 다른 퇴장 규칙을 따릅니다.
     * - 방장은 자신만 채팅방에 남아있는 경우에만 퇴장할 수 있습니다.
     * - 방장이 퇴장하면 채팅방도 함께 삭제됩니다.
     * - 일반 멤버는 언제든지 퇴장할 수 있습니다.
     * </pre>
     *
     * @return ChatRoomLeaveResult 퇴장 처리 결과 (채팅방 삭제 여부 포함)
     * @throws ChatMemberErrorException {@link ChatMemberErrorCode#ADMIN_CANNOT_LEAVE} : 다른 멤버가 있는 상태에서 방장이 퇴장을 시도하는 경우
     */
    public ChatRoomLeaveResult leave() {
        if (!chatMember.isAdmin()) {
            return handleMemberLeave();
        }

        return handleAdminLeave();
    }

    private ChatRoomLeaveResult handleAdminLeave() {
        ChatRoom chatRoom = chatMember.getChatRoom();
        if (!chatRoom.hasOnlyAdmin()) {
            log.warn("채팅방에 사용자가 남아 있다면, 채팅방 방장은 채팅방을 탈퇴할 수 없습니다. chatRoomId: {}, chatMemberId: {}",
                    chatRoom.getId(), chatMember.getId());
            throw new ChatMemberErrorException(ChatMemberErrorCode.ADMIN_CANNOT_LEAVE);
        }

        chatMember.leave();
        log.info("채팅방 방장이 채팅방을 탈퇴합니다. chatRoom: {}, chatMember: {}", chatRoom, chatMember);
        return new ChatRoomLeaveResult(chatMember, true);
    }

    private ChatRoomLeaveResult handleMemberLeave() {
        chatMember.leave();
        log.info("채팅방 멤버가 채팅방을 탈퇴합니다. chatRoom: {}, chatMember: {}", chatMember.getChatRoom(), chatMember);
        return new ChatRoomLeaveResult(chatMember, false);
    }

    public record ChatRoomLeaveResult(
            ChatMember chatMember,
            boolean shouldDeleteChatRoom
    ) {
    }
}
