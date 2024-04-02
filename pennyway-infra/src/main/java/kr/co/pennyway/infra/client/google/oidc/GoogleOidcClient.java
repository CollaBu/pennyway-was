package kr.co.pennyway.infra.client.google.oidc;

import kr.co.pennyway.infra.common.oidc.OauthOidcClient;
import kr.co.pennyway.infra.common.oidc.OidcPublicKeyResponse;
import kr.co.pennyway.infra.config.DefaultFeignConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "GoogleOauthClient",
        url = "${oauth2.client.provider.google.jwks-uri}",
        configuration = DefaultFeignConfig.class,
        qualifiers = "googleOauthClient",
        primary = false
)
public interface GoogleOidcClient extends OauthOidcClient {
    @Override
    @Cacheable(value = "GoogleOauth", cacheManager = "oidcCacheManager")
    @GetMapping("/oauth2/v3/certs")
    OidcPublicKeyResponse getOIDCPublicKey();
}
