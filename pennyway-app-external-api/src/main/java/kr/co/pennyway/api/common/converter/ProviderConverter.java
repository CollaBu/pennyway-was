package kr.co.pennyway.api.common.converter;

import kr.co.pennyway.api.common.exception.ProviderErrorCode;
import kr.co.pennyway.api.common.exception.ProviderException;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import org.springframework.core.convert.converter.Converter;

public class ProviderConverter implements Converter<String, Provider> {
    @Override
    public Provider convert(String provider) {
        try {
            return Provider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ProviderException(ProviderErrorCode.INVALID_PROVIDER);
        }
    }
}
