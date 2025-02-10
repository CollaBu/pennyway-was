package kr.co.pennyway.socket.common.interceptor;

import kr.co.pennyway.socket.common.interceptor.marker.StompExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompExceptionInterceptor extends StompSubProtocolErrorHandler {
    private final List<StompExceptionHandler> interceptors;

    @Override
    @Nullable
    public Message<byte[]> handleClientMessageProcessingError(@Nullable Message<byte[]> clientMessage, Throwable ex) {
        Throwable cause = ex.getCause();

        for (StompExceptionHandler interceptor : interceptors) {
            if (interceptor.canHandle(cause)) {
                log.warn("STOMP client message processing throw({}) catch handler {}", cause.getMessage(), interceptor);
                return interceptor.handle(clientMessage, cause);
            }
        }

        log.error("STOMP client message processing error: {}", ex.getMessage());
        return super.handleClientMessageProcessingError(clientMessage, ex);
    }
}
