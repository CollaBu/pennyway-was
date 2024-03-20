package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.domains.user.type.Visibility;

@Converter
public class VisibilityConverter extends AbstractLegacyEnumAttributeConverter<Visibility> {
    private static final String ENUM_NAME = "프로필 공개 범위";
    
    public VisibilityConverter(Class<Visibility> targetEnumClass, boolean nullable, String enumName) {
        super(targetEnumClass, nullable, enumName);
    }
}
