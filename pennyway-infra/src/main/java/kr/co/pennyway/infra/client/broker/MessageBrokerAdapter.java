package kr.co.pennyway.infra.client.broker;

import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.core.MessagePostProcessor;

import java.util.Map;

public class MessageBrokerAdapter {
    private final RabbitMessagingTemplate rabbitMessagingTemplate;

    public MessageBrokerAdapter(RabbitMessagingTemplate rabbitMessagingTemplate) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
    }

    public void send(String exchange, String routingKey, Message<?> message) throws MessagingException {
        rabbitMessagingTemplate.send(exchange, routingKey, message);
    }

    public void convertAndSend(String exchange, String routingKey, Object payload) throws MessagingException {
        rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }

    public void convertAndSend(String exchange, String routingKey, Object payload,
                               @Nullable Map<String, Object> headers) throws MessagingException {
        rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload, headers);
    }

    public void convertAndSend(String exchange, String routingKey, Object payload,
                               @Nullable MessagePostProcessor postProcessor) throws MessagingException {
        rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload, postProcessor);
    }

    public void convertAndSend(String exchange, String routingKey, Object payload,
                               @Nullable Map<String, Object> headers, @Nullable MessagePostProcessor postProcessor)
            throws MessagingException {
        rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload, headers, postProcessor);
    }
}
