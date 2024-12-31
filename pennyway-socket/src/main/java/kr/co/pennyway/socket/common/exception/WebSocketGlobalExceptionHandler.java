package kr.co.pennyway.socket.common.exception;

import kr.co.pennyway.common.exception.GlobalErrorException;
import kr.co.pennyway.socket.common.dto.ServerSideMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.security.Principal;
import java.util.Map;

/**
 * 비지니스 예외를 처리하는 전역 핸들러.
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class WebSocketGlobalExceptionHandler {
    private static final String ERROR_DESTINATION = "/queue/errors";
    private static final String MESSAGE_ID = "message-id";

    private final SimpMessagingTemplate template;

    @MessageExceptionHandler(GlobalErrorException.class)
    public void handleGlobalErrorException(Principal principal, StompHeaderAccessor accessor, GlobalErrorException ex) {
        ServerSideMessage serverSideMessage = ServerSideMessage.of(ex.causedBy().getCode(), ex.getBaseErrorCode().getExplainError());
        log.warn("handleGlobalErrorException: {}", serverSideMessage);

        sendErrorMessage(principal, serverSideMessage, accessor.getFirstNativeHeader(MESSAGE_ID));
    }

    @MessageExceptionHandler(Exception.class)
    public void handleException(Principal principal, StompHeaderAccessor accessor, Exception ex) {
        ServerSideMessage serverSideMessage = ServerSideMessage.of("5000", ex.getMessage());
        log.error("handleException: {}", serverSideMessage);

        sendErrorMessage(principal, serverSideMessage, accessor.getFirstNativeHeader(MESSAGE_ID));
    }

    private void sendErrorMessage(Principal principal, ServerSideMessage serverSideMessage, String messageId) {
        if (principal == null) {
            log.warn("예외 메시지를 반환할 사용자가 없습니다.");
            return;
        }

        if (messageId == null) {
            template.convertAndSendToUser(principal.getName(), ERROR_DESTINATION, serverSideMessage);
        } else {
            template.convertAndSendToUser(principal.getName(), ERROR_DESTINATION, serverSideMessage, Map.of(MESSAGE_ID, messageId));
        }
    }
}
