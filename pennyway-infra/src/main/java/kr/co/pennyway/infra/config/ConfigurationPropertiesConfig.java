package kr.co.pennyway.infra.config;

import kr.co.pennyway.infra.common.properties.AppleOidcProperties;
import kr.co.pennyway.infra.common.properties.GoogleOidcProperties;
import kr.co.pennyway.infra.common.properties.KakaoOidcProperties;
import kr.co.pennyway.infra.common.properties.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties({
        ServerProperties.class,
        AppleOidcProperties.class,
        GoogleOidcProperties.class,
        KakaoOidcProperties.class
})
@Configuration
public class ConfigurationPropertiesConfig {
}
