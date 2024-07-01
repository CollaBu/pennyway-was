package kr.co.pennyway.api.common.converter;

import kr.co.pennyway.api.common.query.SpendingCategoryType;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorCode;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorException;
import org.springframework.core.convert.converter.Converter;

public class SpendingCategoryTypeConverter implements Converter<String, SpendingCategoryType> {
    @Override
    public SpendingCategoryType convert(String type) {
        try {
            return SpendingCategoryType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SpendingErrorException(SpendingErrorCode.INVALID_CATEGORY_TYPE);
        }
    }
}
