package kr.co.pennyway.api.common.query;

import kr.co.pennyway.domain.common.redis.phone.PhoneCodeKeyType;
import kr.co.pennyway.domain.domains.oauth.type.Provider;

public enum VerificationType {
    GENERAL("general"),
    OAUTH("oauth"),
    USERNAME("username"),
    PASSWORD("password"),
    PHONE("phone");

    private final String type;

    VerificationType(String type) {
        this.type = type;
    }

    public PhoneCodeKeyType toPhoneVerificationType(Provider provider) {
        return switch (this) {
            case OAUTH -> PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider);
            case USERNAME -> PhoneCodeKeyType.FIND_USERNAME;
            case PASSWORD -> PhoneCodeKeyType.FIND_PASSWORD;
            default -> PhoneCodeKeyType.SIGN_UP;
        };
    }
}
