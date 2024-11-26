package kr.co.pennyway.domain.domains.target.exception;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TargetAmountErrorCode implements BaseErrorCode {
    /* 400 BAD_REQUEST */
    INVALID_TARGET_AMOUNT_DATE(StatusCode.BAD_REQUEST, ReasonCode.INVALID_REQUEST, "당월 목표 금액에 대한 요청이 아닙니다."),

    /* 404 NOT_FOUND */
    NOT_FOUND_TARGET_AMOUNT(StatusCode.NOT_FOUND, ReasonCode.REQUESTED_RESOURCE_NOT_FOUND, "해당 월의 목표 금액이 존재하지 않습니다."),

    /* 409 Conflict */
    ALREADY_EXIST_TARGET_AMOUNT(StatusCode.CONFLICT, ReasonCode.RESOURCE_ALREADY_EXISTS, "이미 해당 월의 목표 금액 데이터가 존재합니다."),
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
