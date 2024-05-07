package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;

@Converter
public class SpendingIconConverter extends AbstractLegacyEnumAttributeConverter<SpendingCategory> {
    private static final String ENUM_NAME = "지출 아이콘";

    public SpendingIconConverter() {
        super(SpendingCategory.class, false, ENUM_NAME);
    }
}
