package kr.co.pennyway.infra.common.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "pennyway.rabbitmq.chat")
public class ChatExchangeProperties {
    private final String exchange;
    private final String queue;
    private final String routingKey;

    @Override
    public String toString() {
        return "ChatExchangeProperties{" +
                "exchange='" + exchange + '\'' +
                ", queue='" + queue + '\'' +
                ", routingKey='" + routingKey + '\'' +
                '}';
    }
}
