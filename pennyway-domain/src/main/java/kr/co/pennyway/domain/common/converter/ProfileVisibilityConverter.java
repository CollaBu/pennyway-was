package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;

@Converter
public class ProfileVisibilityConverter extends AbstractLegacyEnumAttributeConverter<ProfileVisibility> {
    private static final String ENUM_NAME = "프로필 공개 범위";

    public ProfileVisibilityConverter() {
        super(ProfileVisibility.class, false, ENUM_NAME);
    }
}
