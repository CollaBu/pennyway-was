package kr.co.pennyway.socket.common.exception;

import kr.co.pennyway.common.exception.GlobalErrorException;
import kr.co.pennyway.socket.common.dto.ServerSideMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.security.Principal;

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
    public void handleGlobalErrorException(Principal principal, GlobalErrorException ex) {
        ServerSideMessage serverSideMessage = ServerSideMessage.of(ex.causedBy().getCode(), ex.getBaseErrorCode().getExplainError());
        log.error("handleGlobalErrorException: {}", serverSideMessage);

        template.convertAndSendToUser(principal.getName(), ERROR_DESTINATION, serverSideMessage);
    }
}
