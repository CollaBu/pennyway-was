package kr.co.pennyway.common.exception;

/**
 * 커스텀 에러 코드를 정의하기 위한 인터페이스
 * <br>
 * 에러 코드는 다음과 같은 형식으로 정의한다.
 * <br>
 * <pre>
 * {@code
 * @Getter
 * @RequiredArgsConstructor
 * public enum UserErrorCode implements BaseErrorCode {
 *      NOT_FOUND_EMAIL(StatusCode.NOT_FOUND, ReasonCode.ACCESS_TO_THE_REQUESTED_RESOURCE_IS_FORBIDDEN, FieldCode.EMAIL, "해당 유저의 이메일이 존재하지 않습니다."),
 *      NOT_FOUND_USER(StatusCode.NOT_FOUND, ReasonCode.ACCESS_TO_THE_REQUESTED_RESOURCE_IS_FORBIDDEN, FieldCode.ZERO, "해당 유저가 존재하지 않습니다."),
 *
 *      REQUIRED_EMAIL(StatusCode.UNPROCESSABLE_CONTENT, ReasonCode.REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY, FieldCode.EMAIL, "이메일은 필수 입력값입니다."),
 *
 *      private final StatusCode statusCode;
 *      private final ReasonCode reasonCode;
 *      private final FieldCode fieldCode;
 *      private final String message;
 *
 *      @Override
 *      public CausedBy causedBy() {
 *          return CausedBy.valueOf(statusCode, reasonCode, DomainCode.USER, FieldCode.COMMON);
 *      }
 *
 *      @Override
 *      public String getExplainError() throws NoSuchFieldError {
 *          return message;
 *      }
 * }
 * }
 * </pre>
 * 에러 코드는 다음과 같은 형식으로 사용한다.
 * <pre>
 * {@code throw new GlobalErrorException(UserErrorCode.NOT_FOUND_USER); }
 * </pre>
 * See Also:
 * {@link CausedBy}, {@link StatusCode}, {@link ReasonCode}, {@link DomainCode}, {@link FieldCode}, {@link GlobalErrorException}
 * @author YANG JAESEO
 */
public interface BaseErrorCode {
    CausedBy causedBy();
    String getExplainError() throws NoSuchFieldError;
}
