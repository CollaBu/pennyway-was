package kr.co.pennyway.domain.context.chat.dto;

import org.springframework.lang.NonNull;

public record ChatMemberJoinCommand(
        @NonNull Long userId,
        @NonNull Long chatRoomId,
        Integer password
) {
    public static ChatMemberJoinCommand of(@NonNull Long userId, @NonNull Long chatRoomId, Integer password) {
        return new ChatMemberJoinCommand(userId, chatRoomId, password);
    }
}
