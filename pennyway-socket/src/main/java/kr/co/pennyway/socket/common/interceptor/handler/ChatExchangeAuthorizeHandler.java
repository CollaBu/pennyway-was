package kr.co.pennyway.socket.common.interceptor.handler;

import kr.co.pennyway.infra.common.properties.ChatExchangeProperties;
import kr.co.pennyway.socket.common.exception.InterceptorErrorCode;
import kr.co.pennyway.socket.common.exception.InterceptorErrorException;
import kr.co.pennyway.socket.common.interceptor.marker.SubscribeCommandHandler;
import kr.co.pennyway.socket.common.properties.MessageBrokerProperties;
import kr.co.pennyway.socket.common.registry.ResourceAccessRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties({ChatExchangeProperties.class, MessageBrokerProperties.class})
public class ChatExchangeAuthorizeHandler implements SubscribeCommandHandler {
    private static final String REQUEST_EXCHANGE_PREFIX = "/sub/";
    private static final String CONVERTED_EXCHANGE_PREFIX = "/exchange/";

    private final ChatExchangeProperties chatExchangeProperties;
    private final MessageBrokerProperties messageBrokerProperties;

    private final ResourceAccessRegistry resourceAccessRegistry;

    @Override
    public boolean isSupport(StompCommand command) {
        return StompCommand.SUBSCRIBE.equals(command);
    }

    @Override
    public void handle(Message<?> message, StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        // private exchange에 대해서는 bypass
        if (destination != null && destination.startsWith(messageBrokerProperties.getUserPrefix() + "/")) {
            log.info("[Exchange 권한 검사] User {}에 대한 {} 권한 검사 통과 (bypass)", accessor.getUser().getName(), destination);
            return;
        }

        if (resourceAccessRegistry.getChecker(destination).hasPermission(destination, accessor.getUser())) {
            log.info("[Exchange 권한 검사] User {}에 대한 {} 권한 검사 통과", accessor.getUser().getName(), destination);
            String convertedDestination = convertDestination(destination);
            accessor.setDestination(convertedDestination);
        } else {
            log.warn("[Exchange 권한 검사] User {}에 대한 {} 권한 검사 실패", accessor.getUser().getName(), destination);
            throw new InterceptorErrorException(InterceptorErrorCode.UNAUTHORIZED_TO_SUBSCRIBE);
        }
    }

    private String convertDestination(String destination) {
        if (destination == null || !destination.startsWith(REQUEST_EXCHANGE_PREFIX)) {
            throw new InterceptorErrorException(InterceptorErrorCode.INVALID_DESTINATION);
        }

        String convertedDestination = destination.replace(REQUEST_EXCHANGE_PREFIX, CONVERTED_EXCHANGE_PREFIX + chatExchangeProperties.getExchange() + "/");
        log.debug("[Exchange 변환 핸들러] destination={}, convertedDestination={}", destination, convertedDestination);

        return convertedDestination;
    }
}