package kr.co.pennyway.domain.context.chat.dto;

public record ChatRoomToggleCommand(
        Long userId,
        Long chatRoomId
) {
    public ChatRoomToggleCommand {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (chatRoomId == null) {
            throw new IllegalArgumentException("chatRoomId must not be null");
        }
    }

    public static ChatRoomToggleCommand of(Long userId, Long chatRoomId) {
        return new ChatRoomToggleCommand(userId, chatRoomId);
    }
}
