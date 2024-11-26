package kr.co.pennyway.api.common.query;

import jakarta.annotation.Nonnull;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.phone.type.PhoneCodeKeyType;

import static kr.co.pennyway.domain.domains.phone.type.PhoneCodeKeyType.*;

public enum VerificationType {
    GENERAL("general", PhoneCodeKeyType.SIGN_UP),
    OAUTH("oauth", null),
    USERNAME("username", PhoneCodeKeyType.FIND_USERNAME),
    PASSWORD("password", PhoneCodeKeyType.FIND_PASSWORD),
    PHONE("phone", PhoneCodeKeyType.PHONE);

    private final String type;
    private final PhoneCodeKeyType phoneCodeKeyType;

    VerificationType(String type, PhoneCodeKeyType phoneCodeKeyType) {
        this.type = type;
        this.phoneCodeKeyType = phoneCodeKeyType;
    }

    public PhoneCodeKeyType toPhoneVerificationType(@Nonnull Provider provider) {
        if (this.equals(OAUTH)) {
            return switch (provider) {
                case KAKAO -> OAUTH_SIGN_UP_KAKAO;
                case GOOGLE -> OAUTH_SIGN_UP_GOOGLE;
                case APPLE -> OAUTH_SIGN_UP_APPLE;
            };
        }

        return phoneCodeKeyType;
    }
}
