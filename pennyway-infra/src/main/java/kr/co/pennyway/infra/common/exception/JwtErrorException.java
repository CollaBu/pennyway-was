package kr.co.pennyway.infra.common.exception;

import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.GlobalErrorException;

public class JwtErrorException extends GlobalErrorException {
    private final JwtErrorCode errorCode;

    public JwtErrorException(JwtErrorCode jwtErrorCode) {
        super(jwtErrorCode);
        this.errorCode = jwtErrorCode;
    }

    public CausedBy causedBy() {
        return errorCode.causedBy();
    }

    public JwtErrorCode getErrorCode() {
        return errorCode;
    }
}
