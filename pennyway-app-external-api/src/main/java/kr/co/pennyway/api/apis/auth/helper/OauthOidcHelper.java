package kr.co.pennyway.api.apis.auth.helper;

import kr.co.pennyway.common.annotation.Helper;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.infra.client.apple.oidc.AppleOidcClient;
import kr.co.pennyway.infra.client.google.oidc.GoogleOidcClient;
import kr.co.pennyway.infra.client.kakao.oidc.KakaoOidcClient;
import kr.co.pennyway.infra.common.oidc.*;
import kr.co.pennyway.infra.common.properties.AppleOidcProperties;
import kr.co.pennyway.infra.common.properties.GoogleOidcProperties;
import kr.co.pennyway.infra.common.properties.KakaoOidcProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Helper
@Slf4j
public class OauthOidcHelper {
    private final OauthOidcProvider oauthOidcProvider;
    private final Map<Provider, Map<OauthOidcClient, OauthOidcClientProperties>> oauthOidcClients;

    public OauthOidcHelper(
            OauthOidcProvider oauthOidcProvider,
            KakaoOidcClient kakaoOauthClient,
            GoogleOidcClient googleOauthClient,
            AppleOidcClient appleOauthClient,
            KakaoOidcProperties kakaoOauthClientProperties,
            GoogleOidcProperties googleOauthClientProperties,
            AppleOidcProperties appleOauthClientProperties
    ) {
        this.oauthOidcProvider = oauthOidcProvider;
        oauthOidcClients = Map.of(
                Provider.KAKAO, Map.of(kakaoOauthClient, kakaoOauthClientProperties),
                Provider.GOOGLE, Map.of(googleOauthClient, googleOauthClientProperties),
                Provider.APPLE, Map.of(appleOauthClient, appleOauthClientProperties)
        );
    }

    /**
     * Provider에 따라 Client와 Properties를 선택하고 Odic public key 정보를 가져와서 ID Token의 payload를 추출하는 메서드
     *
     * @param provider : {@link Provider}
     * @param idToken  : idToken
     * @param nonce    : 인증 서버 로그인 요청 시 전달한 임의의 문자열
     * @return OIDCDecodePayload : ID Token의 payload
     */
    public OidcDecodePayload getPayload(Provider provider, String idToken, String nonce) {
        OauthOidcClient client = oauthOidcClients.get(provider).keySet().iterator().next();
        OauthOidcClientProperties properties = oauthOidcClients.get(provider).values().iterator().next();
        OidcPublicKeyResponse response = client.getOidcPublicKey();

        return getPayloadFromIdToken(idToken, properties.getIssuer(), properties.getSecret(), nonce, response);
    }

    /**
     * ID Token의 payload를 추출하는 메서드 <br/>
     * OAuth 2.0 spec에 따라 ID Token의 유효성 검사 수행 <br/>
     *
     * @param idToken  : idToken
     * @param iss      : ID Token을 발급한 provider의 URL
     * @param aud      : ID Token이 발급된 앱의 앱 키
     * @param nonce    : 인증 서버 로그인 요청 시 전달한 임의의 문자열 (Optional, 현재는 사용하지 않음)
     * @param response : 공개키 목록
     * @return OIDCDecodePayload : ID Token의 payload
     */
    private OidcDecodePayload getPayloadFromIdToken(String idToken, String iss, String aud, String nonce, OidcPublicKeyResponse response) {
        String kid = getKidFromUnsignedIdToken(idToken, iss, aud, nonce);

        OidcPublicKey key = response.getKeys().stream()
                .filter(k -> k.kid().equals(kid))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No matching key found"));
        return oauthOidcProvider.getOIDCTokenBody(idToken, key.n(), key.e());
    }

    private String getKidFromUnsignedIdToken(String token, String iss, String aud, String nonce) {
        return oauthOidcProvider.getKidFromUnsignedTokenHeader(token, iss, aud, nonce);
    }
}
