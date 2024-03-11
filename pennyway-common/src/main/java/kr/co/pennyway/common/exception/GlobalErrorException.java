package kr.co.pennyway.common.exception;

import lombok.Getter;

/**
 * 전역 에러 처리를 위한 예외 클래스
 * <br>
 * 전역 에러 처리를 명시적인 에러 처리로 변경하고 싶다면 다음과 같이 사용한다.
 * <pre>
 * {@code
 * public class DomainErrorException extends GlobalErrorException {
 *    private final DomainErrorCode errorCode;
 *
 *    public DomainErrorException(DomainErrorCode errorCode) {
 *      super(errorCode);
 *      this.errorCode = errorCode;
 *    }
 *
 *    public CausedBy causedBy() { return errorCode.causedBy(); }
 *    public BaseErrorCode getErrorCode() { return errorCode; }
 * }
 * }
 * </pre>
 * @author YANG JAESEO
 */
@Getter
public class GlobalErrorException extends RuntimeException {
    private final BaseErrorCode baseErrorCode;

    public GlobalErrorException(BaseErrorCode baseErrorCode) {
        super(baseErrorCode.causedBy().reasonCode().name());
        this.baseErrorCode = baseErrorCode;
    }

    public CausedBy causedBy() {
        return baseErrorCode.causedBy();
    }

    @Override
    public String toString() {
        return "GlobalErrorException(code=" + baseErrorCode.causedBy().getCode()
                + ", message=" + baseErrorCode.getExplainError() + ")";
    }
}
