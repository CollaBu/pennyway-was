package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.domains.notification.type.NoticeType;

@Converter
public class NoticeTypeConverter extends AbstractLegacyEnumAttributeConverter<NoticeType> {
    private static final String ENUM_NAME = "알림 타입";

    public NoticeTypeConverter() {
        super(NoticeType.class, false, ENUM_NAME);
    }
}
