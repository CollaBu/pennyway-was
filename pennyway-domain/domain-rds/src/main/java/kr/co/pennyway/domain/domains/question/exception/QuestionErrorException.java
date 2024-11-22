package kr.co.pennyway.domain.domains.question.exception;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.GlobalErrorException;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import lombok.Getter;

public class QuestionErrorException extends GlobalErrorException {
    private final QuestionErrorCode questionErrorCode;

    public QuestionErrorException(QuestionErrorCode questionErrorCode) {
        super(questionErrorCode);
        this.questionErrorCode = questionErrorCode;
    }

    public CausedBy causedBy() {
        return questionErrorCode.causedBy();
    }

    public String getExplainError() {
        return questionErrorCode.getExplainError();
    }
}
