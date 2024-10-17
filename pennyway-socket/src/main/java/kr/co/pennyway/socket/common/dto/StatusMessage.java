package kr.co.pennyway.socket.common.dto;

import kr.co.pennyway.domain.common.redis.session.UserStatus;

import java.util.Objects;

public record StatusMessage(
        UserStatus status,
        Long chatRoomId
) {
    public StatusMessage {
        Objects.requireNonNull(status, "status must not be null");

        if (status.equals(UserStatus.ACTIVE_CHAT_ROOM)) {
            Objects.requireNonNull(chatRoomId, "chatRoomId must not be null");
        }
    }

    public boolean isChatRoomStatus() {
        return status.equals(UserStatus.ACTIVE_CHAT_ROOM);
    }
}
