package kr.co.pennyway.infra.common.exception;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StorageErrorCode implements BaseErrorCode {
    // 400 Bad Request
    MISSING_REQUIRED_PARAMETER(StatusCode.BAD_REQUEST, ReasonCode.MISSING_REQUIRED_PARAMETER, "필수 파라미터가 누락되었습니다."),
    INVALID_EXTENSION(StatusCode.BAD_REQUEST, ReasonCode.MALFORMED_PARAMETER, "지원하지 않는 확장자입니다."),
    INVALID_TYPE(StatusCode.BAD_REQUEST, ReasonCode.MALFORMED_PARAMETER, "지원하지 않는 타입입니다."),
    INVALID_FILE(StatusCode.BAD_REQUEST, ReasonCode.MALFORMED_PARAMETER, "올바르지 않은 파일입니다."),

    // 404 Not Found
    NOT_FOUND(StatusCode.NOT_FOUND, ReasonCode.REQUESTED_RESOURCE_NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),

    // 422 Unprocessable Entity
    INVALID_IMAGE_PATH(StatusCode.UNPROCESSABLE_CONTENT, ReasonCode.MALFORMED_PARAMETER, "올바르지 않은 이미지 경로입니다."),
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
