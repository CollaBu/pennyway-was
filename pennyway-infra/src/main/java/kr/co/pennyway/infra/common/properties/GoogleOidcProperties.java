package kr.co.pennyway.infra.common.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "oauth2.client.provider.google")
public class GoogleOidcProperties {
    private String jwksUri;
    private String clientSecret;
}
