package kr.co.pennyway.socket.common.exception;

import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.GlobalErrorException;

public class InterceptorErrorException extends GlobalErrorException {
    private final InterceptorErrorCode errorCode;

    public InterceptorErrorException(InterceptorErrorCode baseErrorCode) {
        super(baseErrorCode);
        this.errorCode = baseErrorCode;
    }

    public CausedBy causedBy() {
        return errorCode.causedBy();
    }

    public InterceptorErrorCode getErrorCode() {
        return errorCode;
    }
}
