package kr.co.pennyway.common.response.handler;

import kr.co.pennyway.common.exception.GlobalErrorException;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import kr.co.pennyway.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

import static kr.co.pennyway.common.exception.ReasonCode.TYPE_MISMATCH_ERROR_IN_REQUEST_BODY;

/**
 * Controller 하위 계층에서 발생하는 전역 예외를 처리하는 클래스
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Pennyway Custom Exception을 처리하는 메서드
     * @see kr.co.pennyway.common.exception.GlobalErrorException
     */
    @ExceptionHandler(GlobalErrorException.class)
    protected ResponseEntity<ErrorResponse> handleGlobalErrorException(GlobalErrorException e) {
        log.warn("handleGlobalErrorException : {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(e.getBaseErrorCode().causedBy().getCode(), e.getBaseErrorCode().getExplainError());
        return ResponseEntity.status(e.getBaseErrorCode().causedBy().reasonCode().getCode()).body(response);
    }

    /**
     * API 호출 시 인가 관련 예외를 처리하는 메서드
     * @see AccessDeniedException
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    protected ErrorResponse handleAccessDeniedException(AccessDeniedException e) {
        log.warn("handleAccessDeniedException : {}", e.getMessage());
        return ErrorResponse.of(String.valueOf(StatusCode.FORBIDDEN.getCode()), e.getMessage());
    }

    /**
     * API 호출 시 객체 혹은 파라미터 데이터 값이 유효하지 않은 경우
     * @see MethodArgumentNotValidException
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("handleMethodArgumentNotValidException: {}", e.getMessage());
        BindingResult bindingResult = e.getBindingResult();
        ErrorResponse response = ErrorResponse.failure(bindingResult, ReasonCode.REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY);
        return ResponseEntity.unprocessableEntity().body(response);
    }

    /**
     * API 호출 시 객체 혹은 파라미터 데이터 값이 유효하지 않은 경우
     * @see MethodArgumentTypeMismatchException
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("handleMethodArgumentTypeMismatchException: {}", e.getMessage());

        Class<?> type = e.getRequiredType();
        assert type != null;

        Map<String, String> fieldErrors = new HashMap<>();
        if(type.isEnum()){
            fieldErrors.put(e.getName(), "The parameter " + e.getName() + " must have a value among : " + StringUtils.join(type.getEnumConstants(), ", "));
        } else{
            fieldErrors.put(e.getName(), "The parameter " + e.getName() + " must have a value of type " + type.getSimpleName());
        }

        String code = String.valueOf(StatusCode.UNPROCESSABLE_CONTENT.getCode()*10 + TYPE_MISMATCH_ERROR_IN_REQUEST_BODY.getCode());
        ErrorResponse response = ErrorResponse.failure(code, TYPE_MISMATCH_ERROR_IN_REQUEST_BODY.name(), fieldErrors);
        return ResponseEntity.unprocessableEntity().body(response);
    }

    /**
     * API 호출 시 'Header' 내에 데이터 값이 유효하지 않은 경우
     * @see MissingRequestHeaderException
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    protected ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        log.warn("handleMissingRequestHeaderException : {}", e.getMessage());

        String code = String.valueOf(StatusCode.BAD_REQUEST.getCode()*10 + ReasonCode.MISSING_REQUIRED_PARAMETER.getCode());
        final ErrorResponse response = ErrorResponse.of(code, e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
