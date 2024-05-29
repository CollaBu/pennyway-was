package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.domains.sign.type.IpAddressHeader;

@Converter
public class IpAddressHeaderConverter extends AbstractLegacyEnumAttributeConverter<IpAddressHeader> {
    private static final String ENUM_NAME = "IP 주소 헤더";

    public IpAddressHeaderConverter() {
        super(IpAddressHeader.class, false, ENUM_NAME);
    }
}
