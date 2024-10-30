package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.common.redis.message.type.MessageCategoryType;

@Converter
public class MessageCategoryTypeConverter extends AbstractLegacyEnumAttributeConverter<MessageCategoryType> {
    private static final String ENUM_NAME = "메시지 카테고리 타입";

    public MessageCategoryTypeConverter() {
        super(MessageCategoryType.class, false, ENUM_NAME);
    }
}
