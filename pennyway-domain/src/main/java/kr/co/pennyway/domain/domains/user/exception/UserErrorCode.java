package kr.co.pennyway.domain.domains.user.exception;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
    /* 400 BAD_REQUEST */
    ALREADY_SIGNUP(StatusCode.BAD_REQUEST, ReasonCode.INVALID_REQUEST, "이미 회원가입한 유저입니다."),

    /* 401 UNAUTHORIZED */
    NOT_MATCHED_PASSWORD(StatusCode.UNAUTHORIZED, ReasonCode.MISSING_OR_INVALID_AUTHENTICATION_CREDENTIALS, "비밀번호가 일치하지 않습니다."),
    INVALID_USERNAME_OR_PASSWORD(StatusCode.UNAUTHORIZED, ReasonCode.MISSING_OR_INVALID_AUTHENTICATION_CREDENTIALS, "유효하지 않은 아이디 또는 비밀번호입니다."),

    /* 403 FORBIDDEN */
    ALREADY_WITHDRAWAL(StatusCode.FORBIDDEN, ReasonCode.ACCESS_TO_THE_REQUESTED_RESOURCE_IS_FORBIDDEN, "이미 탈퇴한 유저입니다."),

    /* 404 NOT_FOUND */
    NOT_FOUND(StatusCode.NOT_FOUND, ReasonCode.REQUESTED_RESOURCE_NOT_FOUND, "유저를 찾을 수 없습니다."),

    /* 422 UNPROCESSABLE_ENTITY */
    INVALID_NOTIFY_TYPE(StatusCode.UNPROCESSABLE_CONTENT, ReasonCode.TYPE_MISMATCH_ERROR_IN_REQUEST_BODY, "유효하지 않은 알림 타입입니다.");

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
