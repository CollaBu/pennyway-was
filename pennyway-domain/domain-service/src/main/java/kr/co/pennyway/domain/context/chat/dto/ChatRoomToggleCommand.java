package kr.co.pennyway.domain.context.chat.dto;

public record ChatRoomToggleCommand(
        Long userId,
        Long chatRoomId
) {
}
