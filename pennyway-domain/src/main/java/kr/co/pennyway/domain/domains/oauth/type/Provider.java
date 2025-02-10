package kr.co.pennyway.domain.domains.oauth.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import kr.co.pennyway.domain.common.converter.LegacyCommonType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Provider implements LegacyCommonType {
    KAKAO("1", "카카오"),
    GOOGLE("2", "구글"),
    APPLE("3", "애플");

    private final String code;
    private final String type;

    @JsonCreator
    public Provider fromString(String type) {
        return valueOf(type.toUpperCase());
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
        return name().toLowerCase();
    }
}
