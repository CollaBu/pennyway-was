package kr.co.pennyway.domain.domains.question.exception;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum QuestionErrorCode implements BaseErrorCode {
    INTERNAL_MAIL_ERROR(StatusCode.INTERNAL_SERVER_ERROR, ReasonCode.UNEXPECTED_ERROR, "메일 발송에 실패했습니다.");

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
