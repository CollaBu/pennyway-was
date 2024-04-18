package kr.co.pennyway.domain.domains.device.exception;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DeviceErrorCode implements BaseErrorCode {
    /* 400 BAD_REQUEST */
    NOT_MATCH_DEVICE(StatusCode.BAD_REQUEST, ReasonCode.INVALID_REQUEST, "토큰의 디바이스 정보와 일치하지 않습니다."),

    /* 404 NOT_FOUND */
    NOT_FOUND_DEVICE(StatusCode.NOT_FOUND, ReasonCode.REQUESTED_RESOURCE_NOT_FOUND, "디바이스를 찾을 수 없습니다.");

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
