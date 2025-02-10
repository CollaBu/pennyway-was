package kr.co.pennyway.socket.common.interceptor;

import kr.co.pennyway.socket.common.interceptor.marker.StompCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompInboundInterceptor implements ChannelInterceptor {
    private final StompCommandHandlerFactory stompCommandHandlerFactory;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && accessor.getCommand() != null) {
            log.info("[StompInboundInterceptor] command={}", accessor.getCommand());

            for (StompCommandHandler handler : stompCommandHandlerFactory.getHandlers(accessor.getCommand())) {
                handler.handle(message, accessor);
            }
        }

        return message;
    }
}