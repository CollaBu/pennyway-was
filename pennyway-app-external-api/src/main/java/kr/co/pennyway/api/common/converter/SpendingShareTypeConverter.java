package kr.co.pennyway.api.common.converter;

import kr.co.pennyway.api.common.query.SpendingShareType;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorCode;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorException;
import org.springframework.core.convert.converter.Converter;

public class SpendingShareTypeConverter implements Converter<String, SpendingShareType> {
    @Override
    public SpendingShareType convert(String type) {
        try {
            return SpendingShareType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SpendingErrorException(SpendingErrorCode.INVALID_SHARE_TYPE);
        }
    }
}
