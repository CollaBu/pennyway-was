package kr.co.pennyway.socket.config;

import kr.co.pennyway.socket.common.properties.ChatServerProperties;
import kr.co.pennyway.socket.common.properties.MessageBrokerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
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
                .setRelayHost(messageBrokerProperties.getHost())
                .setRelayPort(messageBrokerProperties.getPort())
                .setSystemLogin(messageBrokerProperties.getSystemId())
                .setSystemPasscode(messageBrokerProperties.getSystemPassword());

        config.setUserDestinationPrefix(messageBrokerProperties.getUserPrefix());
        config.setPathMatcher(new AntPathMatcher("."));
        config.setApplicationDestinationPrefixes(messageBrokerProperties.getPublishExchange());
    }
}
