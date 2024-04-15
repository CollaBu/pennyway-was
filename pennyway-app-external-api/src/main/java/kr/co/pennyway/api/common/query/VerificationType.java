package kr.co.pennyway.api.common.query;

import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationType;
import kr.co.pennyway.domain.domains.oauth.type.Provider;

public enum VerificationType {
    GENERAL("general"),
    OAUTH("oauth"),
    USERNAME("username"),
    PASSWORD("password");

    private final String type;

    VerificationType(String type) {
        this.type = type;
    }

    public PhoneVerificationType toPhoneVerificationType(Provider provider) {
        return switch (this) {
            case OAUTH -> PhoneVerificationType.getOauthSignUpTypeByProvider(provider);
            case USERNAME -> PhoneVerificationType.FIND_USERNAME;
            case PASSWORD -> PhoneVerificationType.FIND_PASSWORD;
            default -> PhoneVerificationType.SIGN_UP;
        };
    }
}
