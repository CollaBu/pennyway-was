package kr.co.pennyway.domain.domains.session.type;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE_APP("1", "앱 활성화"),
    ACTIVE_CHAT_ROOM_LIST("2", "채팅방 리스트 뷰"),
    ACTIVE_CHAT_ROOM("3", "채팅방 뷰"),
    BACKGROUND("4", "백그라운드"),
    INACTIVE("5", "비활성화"),
    ;

    private final String code;
    private final String type;

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return type;
    }
}
