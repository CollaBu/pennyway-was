package kr.co.pennyway.infra.common.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "pennyway.rabbitmq.chat")
public class RabbitMQProperties {
    private final String exchange;
    private final String queue;
    private final String routingKey;
}
