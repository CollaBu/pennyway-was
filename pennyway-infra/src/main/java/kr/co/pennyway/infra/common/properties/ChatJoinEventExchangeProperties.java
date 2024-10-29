package kr.co.pennyway.infra.common.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "pennyway.rabbitmq.chat-join-event")
public class ChatJoinEventExchangeProperties {
    private final String queue;
    private final String routingKey;

    @Override
    public String toString() {
        return "ChatJoinEventExchangeProperties{" +
                "queue='" + queue + '\'' +
                ", routingKey='" + routingKey + '\'' +
                '}';
    }
}
