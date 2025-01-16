package kr.co.pennyway.domain.context.chat.dto;

public record ChatRoomDeleteCommand(
        Long userId,
        Long chatRoomId
) {
    public ChatRoomDeleteCommand {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (chatRoomId == null) {
            throw new IllegalArgumentException("chatRoomId must not be null");
        }
    }

    public static ChatRoomDeleteCommand of(Long userId, Long chatRoomId) {
        return new ChatRoomDeleteCommand(userId, chatRoomId);
    }
}
