package kr.co.pennyway.domain.domains.chatroom.exception;

import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.GlobalErrorException;

public class ChatRoomErrorException extends GlobalErrorException {
    private final ChatRoomErrorCode chatRoomErrorCode;

    public ChatRoomErrorException(ChatRoomErrorCode baseErrorCode) {
        super(baseErrorCode);
        this.chatRoomErrorCode = baseErrorCode;
    }

    public CausedBy causedBy() {
        return chatRoomErrorCode.causedBy();
    }

    public String getExplainError() {
        return chatRoomErrorCode.getExplainError();
    }
}
