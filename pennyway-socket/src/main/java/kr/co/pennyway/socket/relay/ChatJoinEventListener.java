package kr.co.pennyway.socket.relay;

import kr.co.pennyway.infra.common.event.ChatRoomJoinEvent;
import kr.co.pennyway.infra.common.properties.ChatExchangeProperties;
import kr.co.pennyway.socket.command.SendMessageCommand;
import kr.co.pennyway.socket.common.constants.SystemMessageTemplate;
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
    private final ChatMessageSendService chatMessageSendService;

    @RabbitListener(
            containerFactory = "simpleRabbitListenerContainerFactory",
            bindings = @QueueBinding(
                    value = @Queue("${pennyway.rabbitmq.chat-join-event.queue}"),
                    exchange = @Exchange(value = "${pennyway.rabbitmq.chat.exchange}", type = "topic"),
                    key = "${pennyway.rabbitmq.chat-join-event.routing-key}"
            )
    )
    public void handleJoinEvent(ChatRoomJoinEvent event) {
        log.debug("handleJoinEvent: {}", event);

        chatMessageSendService.execute(
                SendMessageCommand.createSystemMessage(event.chatRoomId(), SystemMessageTemplate.JOIN_MESSAGE_FORMAT.convertToMessage(event.userName()))
        );
    }
}
