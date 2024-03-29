package kr.co.pennyway.infra.config;

import kr.co.pennyway.infra.common.properties.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties({
        ServerProperties.class
})
@Configuration
public class ConfigurationPropertiesConfig {
}
