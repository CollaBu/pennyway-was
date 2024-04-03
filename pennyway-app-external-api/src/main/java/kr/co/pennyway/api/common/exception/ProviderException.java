package kr.co.pennyway.api.common.exception;

import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.GlobalErrorException;

public class ProviderException extends GlobalErrorException {
    private final ProviderErrorCode errorCode;

    public ProviderException(ProviderErrorCode errorCode) {
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
