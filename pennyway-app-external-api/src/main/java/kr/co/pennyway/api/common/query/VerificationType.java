package kr.co.pennyway.api.common.query;

import jakarta.annotation.Nonnull;
import kr.co.pennyway.domain.common.redis.phone.PhoneCodeKeyType;
import kr.co.pennyway.domain.domains.oauth.type.Provider;

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
            return PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider);
        }

        return phoneCodeKeyType;
    }
}
