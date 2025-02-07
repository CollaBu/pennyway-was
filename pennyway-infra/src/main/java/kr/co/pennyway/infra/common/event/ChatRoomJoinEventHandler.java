package kr.co.pennyway.infra.common.event;

import kr.co.pennyway.infra.client.broker.MessageBrokerAdapter;
import kr.co.pennyway.infra.common.properties.ChatExchangeProperties;
import kr.co.pennyway.infra.common.properties.ChatJoinEventExchangeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ChatRoomJoinEventHandler {
    private final MessageBrokerAdapter messageBrokerAdapter;
    private final ChatExchangeProperties chatExchangeProperties;
    private final ChatJoinEventExchangeProperties chatJoinEventExchangeProperties;

    @Async
    @EventListener
    public void handle(ChatRoomJoinEvent event) {
        log.debug("handle: {}", event);

        Message<?> message = MessageBuilder.createMessage(event, new MessageHeaders(Map.of()));

        messageBrokerAdapter.send(
                chatExchangeProperties.getExchange(),
                chatJoinEventExchangeProperties.getRoutingKey(),
                message
        );
    }
}
