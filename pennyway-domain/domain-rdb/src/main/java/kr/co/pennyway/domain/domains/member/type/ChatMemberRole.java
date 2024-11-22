package kr.co.pennyway.domain.domains.member.type;

import com.fasterxml.jackson.annotation.JsonValue;
import kr.co.pennyway.domain.common.converter.LegacyCommonType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChatMemberRole implements LegacyCommonType {
    ADMIN("0", "ADMIN"),
    MEMBER("1", "MEMBER");;

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
