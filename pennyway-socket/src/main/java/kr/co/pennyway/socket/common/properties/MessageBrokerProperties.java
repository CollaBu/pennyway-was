package kr.co.pennyway.socket.common.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "message-broker.external")
public class MessageBrokerProperties {
    private final String host;
    private final int port;
    private final String systemId;
    private final String systemPassword;
    private final String userPrefix;
    private final String publishExchange;
}
