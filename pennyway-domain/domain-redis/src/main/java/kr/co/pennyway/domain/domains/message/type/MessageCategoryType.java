package kr.co.pennyway.domain.domains.message.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
public enum MessageCategoryType {
    NORMAL("0", "NORMAL"),
    SYSTEM("1", "SYSTEM"),
    SHARE("2", "SHARE");

    private static final Map<String, MessageCategoryType> stringToEnum = Stream.of(values()).collect(java.util.stream.Collectors.toMap(Object::toString, e -> e));
    private final String code;
    private final String type;

    @JsonCreator
    public static MessageCategoryType fromString(String type) {
        return stringToEnum.get(type.toUpperCase());
    }

    public String getCode() {
        return null;
    }

    @Override
    public String toString() {
        return type;
    }
}
