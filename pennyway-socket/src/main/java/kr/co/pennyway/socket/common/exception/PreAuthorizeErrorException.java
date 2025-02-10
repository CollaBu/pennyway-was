package kr.co.pennyway.socket.common.exception;

import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.GlobalErrorException;

public class PreAuthorizeErrorException extends GlobalErrorException {
    private final PreAuthorizeErrorCode errorCode;

    public PreAuthorizeErrorException(PreAuthorizeErrorCode preAuthorizeErrorCode) {
        super(preAuthorizeErrorCode);
        this.errorCode = preAuthorizeErrorCode;
    }

    public CausedBy causedBy() {
        return errorCode.causedBy();
    }

    public PreAuthorizeErrorCode getErrorCode() {
        return errorCode;
    }
}