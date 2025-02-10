package kr.co.pennyway.domain.domains.user.exception;

import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.GlobalErrorException;

public class UserErrorException extends GlobalErrorException {
    private final UserErrorCode userErrorCode;

    public UserErrorException(UserErrorCode userErrorCode) {
        super(userErrorCode);
        this.userErrorCode = userErrorCode;
    }

    public CausedBy causedBy() {
        return userErrorCode.causedBy();
    }

    public String getExplainError() {
        return userErrorCode.getExplainError();
    }
}
