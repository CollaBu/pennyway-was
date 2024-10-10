package kr.co.pennyway.socket.common.interceptor.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.socket.common.dto.ServerSideMessage;
import kr.co.pennyway.socket.common.exception.InterceptorErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SubscribeExceptionHandler extends AbstractStompExceptionHandler {
    public SubscribeExceptionHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public boolean canHandle(Throwable cause) {
        if (cause instanceof InterceptorErrorException ex) {
            return ex.getErrorCode().isSupportCommand(StompCommand.SUBSCRIBE);
        }
        return false;
    }

    @Override
    protected StompCommand getStompCommand() {
        return StompCommand.RECEIPT;
    }

    @Override
    protected ServerSideMessage getServerSideMessage(Throwable cause) {
        InterceptorErrorException ex = (InterceptorErrorException) cause;
        return ServerSideMessage.of(ex.causedBy().getCode(), ex.getErrorCode().getExplainError());
    }

    @Override
    protected boolean isNullReturnRequired(Message<byte[]> clientMessage) {
        if (clientMessage == null) {
            log.warn("receipt header가 존재하지 않습니다. clientMessage={}", clientMessage);
            return true;
        }

        StompHeaderAccessor accessor = StompHeaderAccessor.getAccessor(clientMessage, StompHeaderAccessor.class);

        if (accessor == null || accessor.getReceipt() == null) {
            log.warn("receipt header가 존재하지 않습니다. accessor={}", accessor);
            return true;
        }

        return false;
    }
}
