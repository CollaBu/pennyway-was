package kr.co.pennyway.api.common.converter;

import kr.co.pennyway.api.common.exception.PhoneVerificationErrorCode;
import kr.co.pennyway.api.common.exception.PhoneVerificationException;
import kr.co.pennyway.api.common.query.VerificationType;
import org.springframework.core.convert.converter.Converter;

public class VerificationTypeConverter implements Converter<String, VerificationType> {
    @Override
    public VerificationType convert(String source) {
        try {
            return VerificationType.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PhoneVerificationException(PhoneVerificationErrorCode.INVALID_VERIFICATION_TYPE);
        }
    }
}
