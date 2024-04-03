package kr.co.pennyway.infra.client.apple.oidc;

import kr.co.pennyway.infra.common.oidc.OauthOidcClient;
import kr.co.pennyway.infra.common.oidc.OidcPublicKeyResponse;
import kr.co.pennyway.infra.config.DefaultFeignConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "AppleOauthClient",
        url = "${oauth2.client.provider.apple.jwks-uri}",
        configuration = DefaultFeignConfig.class,
        qualifiers = "appleOauthClient",
        primary = false
)
public interface AppleOidcClient extends OauthOidcClient {
    @Override
    @Cacheable(value = "AppleOauth", cacheManager = "oidcCacheManager")
    @GetMapping("/auth/keys")
    OidcPublicKeyResponse getOIDCPublicKey();
}
