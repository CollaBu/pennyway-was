package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.domains.notification.type.Announcement;

@Converter
public class AnnouncementConverter extends AbstractLegacyEnumAttributeConverter<Announcement> {
    private static final String ENUM_NAME = "공지 타입";

    public AnnouncementConverter() {
        super(Announcement.class, false, ENUM_NAME);
    }
}
