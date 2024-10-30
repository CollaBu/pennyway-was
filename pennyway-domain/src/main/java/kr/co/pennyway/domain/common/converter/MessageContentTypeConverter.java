package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.common.redis.message.domain.MessageContentType;

@Converter
public class MessageContentTypeConverter extends AbstractLegacyEnumAttributeConverter<MessageContentType> {
    private static final String ENUM_NAME = "메시지 컨텐츠 타입";

    public MessageContentTypeConverter() {
        super(MessageContentType.class, false, ENUM_NAME);
    }
}
