package kr.co.pennyway.infra.common.properties;

import kr.co.pennyway.infra.common.oidc.OauthOidcClientProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "oauth2.client.provider.google")
public class GoogleOidcProperties implements OauthOidcClientProperties {
    private final String jwksUri;
    private final String clientSecret;
}
