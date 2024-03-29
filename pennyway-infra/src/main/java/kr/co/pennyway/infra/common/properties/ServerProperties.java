package kr.co.pennyway.infra.common.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "pennyway.server.domain")
public class ServerProperties {
    private final String local;
    private final String dev;
}
