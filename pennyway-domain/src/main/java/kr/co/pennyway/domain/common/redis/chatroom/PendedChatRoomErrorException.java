package kr.co.pennyway.domain.common.redis.chatroom;

import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.GlobalErrorException;

public class PendedChatRoomErrorException extends GlobalErrorException {
    private final PendedChatRoomErrorCode pendedChatRoomErrorCode;

    public PendedChatRoomErrorException(PendedChatRoomErrorCode pendedChatRoomErrorCode) {
        super(pendedChatRoomErrorCode);
        this.pendedChatRoomErrorCode = pendedChatRoomErrorCode;
    }

    public CausedBy causedBy() {
        return pendedChatRoomErrorCode.causedBy();
    }

    public String getExplainError() {
        return pendedChatRoomErrorCode.getExplainError();
    }
}
