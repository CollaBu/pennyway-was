package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.domains.oauth.type.Provider;

@Converter
public class ProviderConverter extends AbstractLegacyEnumAttributeConverter<Provider> {
    private static final String ENUM_NAME = "제공자";

    public ProviderConverter() {
        super(Provider.class, false, ENUM_NAME);
    }
}
