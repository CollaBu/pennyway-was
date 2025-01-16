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
}
