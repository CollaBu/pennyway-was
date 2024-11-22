package kr.co.pennyway.domain.domains.target.exception;

import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.GlobalErrorException;

public class TargetAmountErrorException extends GlobalErrorException {
    private final TargetAmountErrorCode targetAmountErrorCode;

    public TargetAmountErrorException(TargetAmountErrorCode targetAmountErrorCode) {
        super(targetAmountErrorCode);
        this.targetAmountErrorCode = targetAmountErrorCode;
    }

    public CausedBy causedBy() {
        return targetAmountErrorCode.causedBy();
    }

    public String getExplainError() {
        return targetAmountErrorCode.getExplainError();
    }
}
