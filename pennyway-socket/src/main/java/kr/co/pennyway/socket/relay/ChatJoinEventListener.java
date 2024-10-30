package kr.co.pennyway.socket.relay;

import kr.co.pennyway.domain.common.redis.message.type.MessageCategoryType;
import kr.co.pennyway.domain.common.redis.message.type.MessageContentType;
import kr.co.pennyway.infra.common.event.ChatRoomJoinEvent;
import kr.co.pennyway.infra.common.properties.ChatExchangeProperties;
import kr.co.pennyway.socket.service.ChatMessageSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties({ChatExchangeProperties.class})
public class ChatJoinEventListener {
    private static final String JOIN_MESSAGE_SUFFIX = "님이 입장하셨습니다.";

    private final ChatMessageSendService chatMessageSendService;

    @RabbitListener(
            containerFactory = "simpleRabbitListenerContainerFactory",
            bindings = @QueueBinding(
                    value = @Queue("${pennyway.rabbitmq.chat-join-event.queue}"),
                    exchange = @Exchange(value = "${pennyway.rabbitmq.chat.exchange}"),
                    key = "${pennyway.rabbitmq.chat-join-event.routing-key}"
            )
    )
    public void handleJoinEvent(ChatRoomJoinEvent event) {
        log.debug("handleJoinEvent: {}", event);

        chatMessageSendService.execute(
                event.chatRoomId(),
                event.userName() + JOIN_MESSAGE_SUFFIX,
                MessageContentType.TEXT,
                MessageCategoryType.SYSTEM,
                0L // 시스템 메시지
        );
    }
}
