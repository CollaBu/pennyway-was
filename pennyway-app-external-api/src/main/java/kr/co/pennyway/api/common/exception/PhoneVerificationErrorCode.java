package kr.co.pennyway.api.common.exception;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PhoneVerificationErrorCode implements BaseErrorCode {
    // 401 Unauthorized
    IS_NOT_VALID_CODE(StatusCode.UNAUTHORIZED, ReasonCode.MISSING_OR_INVALID_AUTHENTICATION_CREDENTIALS, "인증코드가 일치하지 않습니다."),

    // 404 Not Found
    EXPIRED_OR_INVALID_PHONE(StatusCode.NOT_FOUND, ReasonCode.RESOURCE_DELETED_OR_MOVED, "만료되었거나 등록되지 않은 휴대폰 정보입니다."),
    ;

    private final StatusCode statusCode;
    private final ReasonCode reasonCode;
    private final String message;

    @Override
    public CausedBy causedBy() {
        return CausedBy.of(statusCode, reasonCode);
    }

    @Override
    public String getExplainError() throws NoSuchFieldError {
        return message;
    }
}
