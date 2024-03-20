package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.domains.user.type.Role;

@Converter
public class RoleConverter extends AbstractLegacyEnumAttributeConverter<Role> {
    private static final String ENUM_NAME = "유저 권한";

    public RoleConverter(Class<Role> targetEnumClass, boolean nullable, String enumName) {
        super(targetEnumClass, nullable, enumName);
    }
}
