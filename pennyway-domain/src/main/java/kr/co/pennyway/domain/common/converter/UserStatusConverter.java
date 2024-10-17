package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.common.redis.session.UserStatus;

@Converter
public class UserStatusConverter extends AbstractLegacyEnumAttributeConverter<UserStatus> {
    private static final String ENUM_NAME = "유저 상태";

    public UserStatusConverter() {
        super(UserStatus.class, false, ENUM_NAME);
    }
}
