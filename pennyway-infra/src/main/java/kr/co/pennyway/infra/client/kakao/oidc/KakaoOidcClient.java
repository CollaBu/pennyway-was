package kr.co.pennyway.infra.client.kakao.oidc;

import kr.co.pennyway.infra.common.oidc.OauthOidcClient;
import kr.co.pennyway.infra.common.oidc.OidcPublicKeyResponse;
import kr.co.pennyway.infra.config.DefaultFeignConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "KakaoOauthClient",
        url = "${oauth2.client.provider.kakao.jwks-uri}",
        configuration = DefaultFeignConfig.class,
        qualifiers = "kakaoOauthClient"
)
public interface KakaoOidcClient extends OauthOidcClient {
    @Override
    @Cacheable(value = "KakaoOauth", cacheManager = "oidcCacheManager")
    @GetMapping("/.well-known/jwks.json")
    OidcPublicKeyResponse getOIDCPublicKey();
}
