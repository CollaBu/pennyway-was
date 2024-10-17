package kr.co.pennyway.domain.common.redis.session;

import kr.co.pennyway.domain.common.converter.LegacyCommonType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserStatus implements LegacyCommonType {
    ACTIVE_APP("1", "앱 활성화"),
    ACTIVE_CHAT_ROOM_LIST("2", "채팅방 리스트 뷰"),
    ACTIVE_CHAT_ROOM("3", "채팅방 뷰"),
    BACKGROUND("4", "백그라운드"),
    INACTIVE("5", "비활성화"),
    ;

    private final String code;
    private final String type;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return type;
    }
}
