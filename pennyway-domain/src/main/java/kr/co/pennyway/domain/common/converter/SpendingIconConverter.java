package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.domains.spending.type.SpendingIcon;

@Converter
public class SpendingIconConverter extends AbstractLegacyEnumAttributeConverter<SpendingIcon> {
    private static final String ENUM_NAME = "지출 아이콘";

    public SpendingIconConverter() {
        super(SpendingIcon.class, false, ENUM_NAME);
    }
}
