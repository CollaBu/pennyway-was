package kr.co.pennyway.domain.domains.user.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import kr.co.pennyway.domain.common.converter.LegacyCommonType;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public enum Role implements LegacyCommonType {
    ADMIN("0", "ROLE_ADMIN"),
    USER("1", "ROLE_USER");

    private static final Map<String, Role> stringToEnum =
            Stream.of(values()).collect(toMap(Object::toString, e -> e));
    private final String code;
    private final String type;

    @JsonCreator
    public static Role fromString(String type) {
        return stringToEnum.get(type.toUpperCase());
    }

    @Override
    public String getCode() {
        return code;
    }

    @JsonValue
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}
