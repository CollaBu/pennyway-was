package kr.co.pennyway.domain.domains.device.exception;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DeviceTokenErrorCode implements BaseErrorCode {
    /* 400 BAD_REQUEST */
    NOT_ACTIVATED_DEVICE(StatusCode.BAD_REQUEST, ReasonCode.CLIENT_ERROR, "활성화되지 않은 디바이스 토큰 정보입니다."),

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
