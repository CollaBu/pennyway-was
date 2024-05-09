package kr.co.pennyway.domain.domains.spending.exception;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpendingErrorCode implements BaseErrorCode {
    /* 400 Bad Request */
    INVALID_ICON(StatusCode.BAD_REQUEST, ReasonCode.INVALID_REQUEST, "OTHER 아이콘은 커스텀 카테고리의 icon으로 사용할 수 없습니다."),

    /* 404 Not Found */
    NOT_FOUND_CUSTOM_CATEGORY(StatusCode.NOT_FOUND, ReasonCode.REQUESTED_RESOURCE_NOT_FOUND, "존재하지 않는 커스텀 카테고리입니다.");

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
