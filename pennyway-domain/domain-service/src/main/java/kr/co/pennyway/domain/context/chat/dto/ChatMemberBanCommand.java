package kr.co.pennyway.domain.context.chat.dto;

public record ChatMemberBanCommand(
        Long userId,
        Long targetMemberId,
        Long chatRoomId
) {
    public ChatMemberBanCommand {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (targetMemberId == null) {
            throw new IllegalArgumentException("targetMemberId must not be null");
        }
        if (chatRoomId == null) {
            throw new IllegalArgumentException("chatRoomId must not be null");
        }
    }

    public static ChatMemberBanCommand of(Long adminId, Long targetMemberId, Long chatRoomId) {
        return new ChatMemberBanCommand(adminId, targetMemberId, chatRoomId);
    }
}
