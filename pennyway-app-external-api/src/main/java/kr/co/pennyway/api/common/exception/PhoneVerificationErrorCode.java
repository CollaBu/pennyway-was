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
    // 400 Bad Request
    INVALID_VERIFICATION_TYPE(StatusCode.BAD_REQUEST, ReasonCode.INVALID_REQUEST, "유효하지 않은 인증 타입입니다."),
    PROVIDER_IS_REQUIRED(StatusCode.BAD_REQUEST, ReasonCode.MISSING_REQUIRED_PARAMETER, "type이 OAUTH인 경우 provider는 필수입니다."),

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
