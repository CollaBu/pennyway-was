package kr.co.pennyway.socket.relay;

import kr.co.pennyway.socket.dto.ChatMessageDto;
import kr.co.pennyway.socket.service.ChatMessageRelayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageRelayEventListener {
    private final ChatMessageRelayService chatMessageRelayService;

    @RabbitListener(
            containerFactory = "simpleRabbitListenerContainerFactory",
            bindings = @QueueBinding(
                    value = @Queue("${pennyway.rabbitmq.chat.queue}"),
                    exchange = @Exchange(value = "${pennyway.rabbitmq.chat.exchange}", type = "topic"),
                    key = "${pennyway.rabbitmq.chat.routing-key}"
            ),
            concurrency = "1"
    )
    public void handleSendEvent(ChatMessageDto.Response event) {
        log.info("ChatMessageSendEventListener.handleSendEvent: {}", event);

        chatMessageRelayService.execute(event.senderId(), event.chatRoomId(), event.content());
    }
}
