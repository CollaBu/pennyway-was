package kr.co.pennyway.api.common.exception;

import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.GlobalErrorException;

public class ApiErrorException extends GlobalErrorException {
    private final ApiErrorCode errorCode;

    public ApiErrorException(ApiErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    @Override
    public CausedBy causedBy() {
        return errorCode.causedBy();
    }

    public String getExplainError() {
        return errorCode.getExplainError();
    }
}
