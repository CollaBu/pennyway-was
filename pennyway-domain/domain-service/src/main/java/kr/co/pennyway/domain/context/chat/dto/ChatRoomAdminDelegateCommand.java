package kr.co.pennyway.domain.context.chat.dto;

public record ChatRoomAdminDelegateCommand(
        Long chatRoomId,
        Long chatAdminUserId,
        Long targetChatMemberId
) {
    public ChatRoomAdminDelegateCommand {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("chatRoomId must not be null");
        }

        if (chatAdminUserId == null) {
            throw new IllegalArgumentException("chatAdminUserId must not be null");
        }

        if (targetChatMemberId == null) {
            throw new IllegalArgumentException("targetChatMemberId must not be null");
        }
    }

    public static ChatRoomAdminDelegateCommand of(Long chatRoomId, Long chatAdminUserId, Long targetChatMemberId) {
        return new ChatRoomAdminDelegateCommand(chatRoomId, chatAdminUserId, targetChatMemberId);
    }
}
