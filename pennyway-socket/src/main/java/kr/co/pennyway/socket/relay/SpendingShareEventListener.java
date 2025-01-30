package kr.co.pennyway.socket.relay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.domain.domains.message.type.MessageCategoryType;
import kr.co.pennyway.domain.domains.message.type.MessageContentType;
import kr.co.pennyway.infra.common.event.SpendingChatShareEvent;
import kr.co.pennyway.infra.common.properties.ChatExchangeProperties;
import kr.co.pennyway.socket.command.SendMessageCommand;
import kr.co.pennyway.socket.service.ChatMessageSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties({ChatExchangeProperties.class})
public class SpendingShareEventListener {
    private final ChatMessageSendService chatMessageSendService;
    private final ObjectMapper objectMapper;

    @RabbitListener(
            containerFactory = "simpleRabbitListenerContainerFactory",
            bindings = @QueueBinding(
                    value = @Queue("${pennyway.rabbitmq.spending-chat-share.queue}"),
                    exchange = @Exchange(value = "${pennyway.rabbitmq.chat.exchange}", type = "topic"),
                    key = "${pennyway.rabbitmq.spending-chat-share.routing-key}"
            )
    )
    public void handle(SpendingChatShareEvent event) {
        log.debug("handle: {}", event);

        var payload = convertToJson(event.spendingOnDates());

        chatMessageSendService.execute(
                SendMessageCommand.createMessage(
                        event.chatRoomId(),
                        payload,
                        MessageContentType.TEXT,
                        MessageCategoryType.SHARE,
                        event.senderId(),
                        event.name(),
                        null,
                        Map.of("Content-Type", "application/json")
                )
        );
    }

    private String convertToJson(List<SpendingChatShareEvent.SpendingOnDate> object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize spendingOnDates", e);
            return null;
        }
    }
}
