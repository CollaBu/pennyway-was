package kr.co.pennyway.socket.common.exception;

import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.GlobalErrorException;

public class MessageErrorException extends GlobalErrorException {
    private final MessageErrorCode errorCode;

    public MessageErrorException(MessageErrorCode baseErrorCode) {
        super(baseErrorCode);
        this.errorCode = baseErrorCode;
    }

    public CausedBy causedBy() {
        return errorCode.causedBy();
    }

    public MessageErrorCode getErrorCode() {
        return errorCode;
    }
}
