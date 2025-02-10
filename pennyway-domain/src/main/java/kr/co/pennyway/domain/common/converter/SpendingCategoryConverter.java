package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;

@Converter
public class SpendingCategoryConverter extends AbstractLegacyEnumAttributeConverter<SpendingCategory> {
    private static final String ENUM_NAME = "지출 카테고리";

    public SpendingCategoryConverter() {
        super(SpendingCategory.class, false, ENUM_NAME);
    }
}
