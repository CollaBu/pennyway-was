package kr.co.pennyway.domain.common.redis.phone;

import kr.co.pennyway.domain.domains.oauth.type.Provider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PhoneCodeKeyType {
    SIGN_UP("signUp"),
    OAUTH_SIGN_UP_KAKAO("oauthSignUp:kakao"),
    OAUTH_SIGN_UP_GOOGLE("oauthSignUp:google"),
    OAUTH_SIGN_UP_APPLE("oauthSignUp:apple"),
    FIND_USERNAME("username"),
    FIND_PASSWORD("password"),
    PHONE("phone");

    private final String prefix;

    public static PhoneCodeKeyType getOauthSignUpTypeByProvider(Provider provider) {
        return switch (provider) {
            case KAKAO -> OAUTH_SIGN_UP_KAKAO;
            case GOOGLE -> OAUTH_SIGN_UP_GOOGLE;
            case APPLE -> OAUTH_SIGN_UP_APPLE;
        };
    }
}
