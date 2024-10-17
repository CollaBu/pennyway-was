package kr.co.pennyway.socket.common.exception;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;

public enum MessageErrorCode implements BaseErrorCode {
    MALFORMED_MESSAGE_BODY(StatusCode.BAD_REQUEST, ReasonCode.MALFORMED_REQUEST_BODY, "잘못된 메시지 형식입니다"),
    ;

    private final StatusCode statusCode;
    private final ReasonCode reasonCode;
    private final String message;

    MessageErrorCode(StatusCode statusCode, ReasonCode reasonCode, String message) {
        this.statusCode = statusCode;
        this.reasonCode = reasonCode;
        this.message = message;
    }

    @Override
    public CausedBy causedBy() {
        return CausedBy.of(statusCode, reasonCode);
    }

    @Override
    public String getExplainError() throws NoSuchFieldError {
        return message;
    }
}
