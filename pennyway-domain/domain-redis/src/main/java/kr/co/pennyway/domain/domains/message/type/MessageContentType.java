package kr.co.pennyway.domain.domains.message.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
public enum MessageContentType {
    TEXT("0", "TEXT"),
    IMAGE("1", "IMAGE"),
    VIDEO("2", "VIDEO"),
    FILE("3", "FILE");

    private static final Map<String, MessageContentType> stringToEnum = Stream.of(values()).collect(java.util.stream.Collectors.toMap(Object::toString, e -> e));
    private final String code;
    private final String type;

    @JsonCreator
    public static MessageContentType fromString(String type) {
        return stringToEnum.get(type.toUpperCase());
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return type;
    }
}
