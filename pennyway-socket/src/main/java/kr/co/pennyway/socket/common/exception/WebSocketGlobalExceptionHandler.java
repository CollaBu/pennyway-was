package kr.co.pennyway.socket.common.exception;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import kr.co.pennyway.common.exception.GlobalErrorException;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import kr.co.pennyway.socket.common.dto.ServerSideMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.security.Principal;

import static kr.co.pennyway.common.exception.ReasonCode.TYPE_MISMATCH_ERROR_IN_REQUEST_BODY;

/**
 * 비지니스 예외를 처리하는 전역 핸들러.
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class WebSocketGlobalExceptionHandler {
    private static final String ERROR_DESTINATION = "/queue/errors";

    private final SimpMessagingTemplate template;

    @MessageExceptionHandler(GlobalErrorException.class)
    public void handleGlobalErrorException(Principal principal, StompHeaderAccessor accessor, GlobalErrorException e) {
        ServerSideMessage serverSideMessage = ServerSideMessage.of(e.causedBy().getCode(), e.getBaseErrorCode().getExplainError());
        log.warn("handleGlobalErrorException: {}", serverSideMessage);

        sendErrorMessage(principal, serverSideMessage, accessor);
    }

    @MessageExceptionHandler(MethodArgumentTypeMismatchException.class)
    public void handleMethodArgumentTypeMismatchException(Principal principal, StompHeaderAccessor accessor, MethodArgumentTypeMismatchException e) {
        String code = String.valueOf(StatusCode.UNPROCESSABLE_CONTENT.getCode() * 10 + TYPE_MISMATCH_ERROR_IN_REQUEST_BODY.getCode());
        ServerSideMessage serverSideMessage = ServerSideMessage.of(code, e.getMessage());
        log.warn("handleMethodArgumentTypeMismatchException: {}", serverSideMessage);

        sendErrorMessage(principal, serverSideMessage, accessor);
    }

    @MessageExceptionHandler(HttpMessageNotReadableException.class)
    public void handleHttpMessageNotReadableException(Principal principal, StompHeaderAccessor accessor, HttpMessageNotReadableException e) {
        String code, message;
        if (e.getCause() instanceof MismatchedInputException mismatchedInputException) {
            code = String.valueOf(StatusCode.UNPROCESSABLE_CONTENT.getCode() * 10 + TYPE_MISMATCH_ERROR_IN_REQUEST_BODY.getCode());
            message = mismatchedInputException.getPath().get(0).getFieldName() + " 필드의 값이 유효하지 않습니다.";
        } else {
            code = String.valueOf(StatusCode.BAD_REQUEST.getCode() * 10 + ReasonCode.MALFORMED_REQUEST_BODY.getCode());
            message = e.getMessage();
        }

        ServerSideMessage serverSideMessage = ServerSideMessage.of(code, message);
        log.warn("handleHttpMessageNotReadableException: {}", serverSideMessage);

        sendErrorMessage(principal, serverSideMessage, accessor);
    }

    @MessageExceptionHandler(Exception.class)
    public void handleException(Principal principal, StompHeaderAccessor accessor, Exception e) {
        ServerSideMessage serverSideMessage = ServerSideMessage.of("5000", e.getMessage());
        log.error("handleException: {}", serverSideMessage);

        sendErrorMessage(principal, serverSideMessage, accessor);
    }

    private void sendErrorMessage(Principal principal, ServerSideMessage serverSideMessage, StompHeaderAccessor accessor) {
        if (principal == null) {
            log.warn("예외 메시지를 반환할 사용자가 없습니다.");
            return;
        }

        template.convertAndSendToUser(principal.getName(), ERROR_DESTINATION, serverSideMessage, accessor.getMessageHeaders());
    }
}
