package kr.co.pennyway.socket.common.dto;

import kr.co.pennyway.domain.domains.session.type.UserStatus;
import kr.co.pennyway.socket.common.exception.MessageErrorCode;
import kr.co.pennyway.socket.common.exception.MessageErrorException;

import java.util.Objects;

public record StatusMessage(
        UserStatus status,
        Long chatRoomId
) {
    public StatusMessage {
        if (Objects.isNull(status)) {
            throw new MessageErrorException(MessageErrorCode.MALFORMED_MESSAGE_BODY);
        }

        if (status.equals(UserStatus.ACTIVE_CHAT_ROOM) && Objects.isNull(chatRoomId)) {
            throw new MessageErrorException(MessageErrorCode.MALFORMED_MESSAGE_BODY);
        }
    }

    public boolean isChatRoomStatus() {
        return status.equals(UserStatus.ACTIVE_CHAT_ROOM);
    }
}
