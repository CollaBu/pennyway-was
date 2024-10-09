package kr.co.pennyway.socket.common.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "pennyway.socket.chat")
public class ChatServerProperties {
    private final String endpoint;
    private final List<String> allowedOriginPatterns;
}
