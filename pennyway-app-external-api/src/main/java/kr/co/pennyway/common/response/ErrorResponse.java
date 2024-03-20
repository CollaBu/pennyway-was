package kr.co.pennyway.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@Getter
@Schema(description = "API 응답 - 실패 및 에러")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {
    @Schema(description = "응답 코드", defaultValue = "4000")
    private String code;
    @Schema(description = "응답 메시지", example = "에러 이유")
    private String message;
    @Schema(description = "에러 상세", example = "{\"field\":\"reason\"}")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object fieldErrors;

    @Builder
    private ErrorResponse(String code, String message, Object fieldErrors) {
        this.code = code;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    /**
     * 단일 필드 에러를 응답으로 변환한다.
     * @param code String : {@link kr.co.pennyway.common.exception.CausedBy} 클래스의 getCode() 메소드로 반환되는 코드
     * @param message : 에러 이유
     * @return ErrorResponse
     */
    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * 422 Unprocessable Entity 관련 에러를 응답으로 변환한다.
     * @param code String : {@link kr.co.pennyway.common.exception.CausedBy} 클래스의 getCode() 메소드로 반환되는 코드
     * @param message : 에러 이유
     * @param fieldErrors : 에러 상세
     * @return ErrorResponse
     */
    public static ErrorResponse failure(String code, String message, Object fieldErrors) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .fieldErrors(fieldErrors)
                .build();
    }

    /**
     * 422 Unprocessable Content 예외에서 발생한 BindingResult를 응답으로 변환한다.
     * @param bindingResult : BindingResult
     * @return ErrorResponse
     */
    public static ErrorResponse failure(BindingResult bindingResult, ReasonCode reasonCode) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        String code = String.valueOf(StatusCode.UNPROCESSABLE_CONTENT.getCode()*10 + reasonCode.getCode());
        return failure(code, StatusCode.UNPROCESSABLE_CONTENT.name(), fieldErrors);
    }
}
