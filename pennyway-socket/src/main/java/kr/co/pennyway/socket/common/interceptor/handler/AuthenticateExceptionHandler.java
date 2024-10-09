package kr.co.pennyway.socket.common.interceptor.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.infra.common.exception.JwtErrorException;
import kr.co.pennyway.infra.common.util.JwtErrorCodeUtil;
import kr.co.pennyway.socket.common.dto.ServerSideMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthenticateExceptionHandler extends AbstractStompExceptionHandler {
    public AuthenticateExceptionHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public boolean canHandle(Throwable cause) {
        return cause instanceof JwtErrorException;
    }

    @Override
    protected StompCommand getStompCommand() {
        return StompCommand.ERROR;
    }

    @Override
    protected ServerSideMessage getServerSideMessage(Throwable cause) {
        JwtErrorException ex = (JwtErrorException) cause;
        ex = JwtErrorCodeUtil.determineAuthErrorException(ex);

        log.warn("[인증 예외] {}", ex.getErrorCode().getMessage());

        return ServerSideMessage.of(ex.getErrorCode().getExplainError());
    }
}
