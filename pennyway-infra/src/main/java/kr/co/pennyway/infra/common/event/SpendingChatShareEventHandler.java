package kr.co.pennyway.infra.common.event;

import kr.co.pennyway.infra.client.broker.MessageBrokerAdapter;
import kr.co.pennyway.infra.common.properties.ChatExchangeProperties;
import kr.co.pennyway.infra.common.properties.SpendingChatShareExchangeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SpendingChatShareEventHandler {
    private final MessageBrokerAdapter messageBrokerAdapter;
    private final ChatExchangeProperties chatExchangeProperties;
    private final SpendingChatShareExchangeProperties spendingChatShareExchangeProperties;

    @Async
    @EventListener
    public void handle(SpendingChatShareEvent event) {
        log.debug("handle: {}", event);

        var headers = new MessageHeaders(Map.of("Content-Type", "application/json"));
        var message = MessageBuilder.createMessage(event, headers);

        messageBrokerAdapter.send(
                chatExchangeProperties.getExchange(),
                spendingChatShareExchangeProperties.getRoutingKey(),
                message
        );
    }
}
