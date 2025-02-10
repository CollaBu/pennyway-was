package kr.co.pennyway.domain.domains.phone.type;

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
}
