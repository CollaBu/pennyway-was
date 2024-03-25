package kr.co.pennyway.domain.common.redis.phone;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PhoneVerificationCode {
    SIGN_UP("signUp"), OAUTH_SIGN_UP("oauthSignUp"), FIND_USERNAME("username"), FIND_PASSWORD("password");

    private final String prefix;
}
