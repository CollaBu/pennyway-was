package kr.co.pennyway.domain.domains.spending.exception;

import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.GlobalErrorException;

public class SpendingErrorException extends GlobalErrorException {
    private final SpendingErrorCode errorCode;

    public SpendingErrorException(SpendingErrorCode errorCode) {
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
