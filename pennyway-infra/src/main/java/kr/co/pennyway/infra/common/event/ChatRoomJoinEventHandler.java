package kr.co.pennyway.infra.common.event;

import kr.co.pennyway.infra.client.broker.MessageBrokerAdapter;
import kr.co.pennyway.infra.common.properties.ChatExchangeProperties;
import kr.co.pennyway.infra.common.properties.ChatJoinEventExchangeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties({ChatExchangeProperties.class, ChatJoinEventExchangeProperties.class})
public class ChatRoomJoinEventHandler {
    private final MessageBrokerAdapter messageBrokerAdapter;
    private final ChatExchangeProperties chatExchangeProperties;
    private final ChatJoinEventExchangeProperties chatJoinEventExchangeProperties;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatRoomJoinEvent event) {
        log.debug("handle: {}", event);

        Message<?> message = MessageBuilder.createMessage(event, null);

        messageBrokerAdapter.send(
                chatExchangeProperties.getExchange(),
                chatJoinEventExchangeProperties.getRoutingKey(),
                message
        );
    }
}
