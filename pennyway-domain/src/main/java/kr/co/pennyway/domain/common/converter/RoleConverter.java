package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.domains.user.type.Role;

@Converter
public class RoleConverter extends AbstractLegacyEnumAttributeConverter<Role> {
    private static final String ENUM_NAME = "유저 권한";

    public RoleConverter() {
        super(Role.class, false, ENUM_NAME);
    }
}
