package kr.co.pennyway.domain.domains.user.type;

import com.fasterxml.jackson.annotation.JsonValue;
import kr.co.pennyway.domain.common.converter.LegacyCommonType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProfileVisibility implements LegacyCommonType {
    PUBLIC("0", "전체 공개"),
    FRIEND("1", "친구 공개"),
    PRIVATE("2", "비공개");

    private final String code;
    private final String type;

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
