package kr.co.pennyway.api.common.converter;

import kr.co.pennyway.domain.common.redis.phone.Code;
import org.springframework.core.convert.converter.Converter;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

public class ValidationCodeConverter implements Converter<String, Code> {
    @Override
    public Code convert(String source) {
        try {
            return Code.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MethodArgumentTypeMismatchException(source, Code.class, "code", null, e);
        }
    }
}
