package kr.co.pennyway.socket.config;

import kr.co.pennyway.socket.common.properties.ChatServerProperties;
import kr.co.pennyway.socket.common.properties.MessageBrokerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompReactorNettyCodec;
import org.springframework.messaging.tcp.reactor.ReactorNettyTcpClient;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import reactor.netty.tcp.TcpClient;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
@EnableConfigurationProperties({ChatServerProperties.class, MessageBrokerProperties.class})
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {
    private final ChatServerProperties chatServerProperties;
    private final MessageBrokerProperties messageBrokerProperties;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(chatServerProperties.getEndpoint())
                .setAllowedOriginPatterns(chatServerProperties.getAllowedOriginPatterns().toArray(new String[0]));
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
                .setAutoStartup(true)
                .setTcpClient(createTcpClient())
                .setSystemLogin(messageBrokerProperties.getSystemId())
                .setSystemPasscode(messageBrokerProperties.getSystemPassword())
                .setClientLogin(messageBrokerProperties.getClientId())
                .setClientPasscode(messageBrokerProperties.getClientPassword())
                .setRelayHost(messageBrokerProperties.getHost())
                .setRelayPort(messageBrokerProperties.getPort());

        config.setUserDestinationPrefix(messageBrokerProperties.getUserPrefix());
        config.setPathMatcher(new AntPathMatcher("."));
        config.setApplicationDestinationPrefixes(messageBrokerProperties.getPublishExchange());
    }

    private ReactorNettyTcpClient<byte[]> createTcpClient() {
        TcpClient tcpClient = TcpClient
                .create()
                .host(messageBrokerProperties.getHost())
                .port(messageBrokerProperties.getPort());

        return new ReactorNettyTcpClient<>(tcpClient, new StompReactorNettyCodec());
    }
}
