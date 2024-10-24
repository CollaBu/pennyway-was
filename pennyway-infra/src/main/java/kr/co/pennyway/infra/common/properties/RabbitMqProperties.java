package kr.co.pennyway.infra.common.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "spring.rabbitmq")
public class RabbitMqProperties {
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String virtualHost;
    private final int requestedHeartbeat;

    @Override
    public String toString() {
        return "RabbitMqProperties{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", virtualHost='" + virtualHost + '\'' +
                ", requestedHeartbeat=" + requestedHeartbeat +
                '}';
    }
}
