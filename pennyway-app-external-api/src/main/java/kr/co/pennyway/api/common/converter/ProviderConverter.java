package kr.co.pennyway.api.common.converter;

import kr.co.pennyway.api.common.exception.OauthErrorCode;
import kr.co.pennyway.api.common.exception.OauthException;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import org.springframework.core.convert.converter.Converter;

public class ProviderConverter implements Converter<String, Provider> {
    @Override
    public Provider convert(String provider) {
        try {
            return Provider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new OauthException(OauthErrorCode.INVALID_PROVIDER);
        }
    }
}
