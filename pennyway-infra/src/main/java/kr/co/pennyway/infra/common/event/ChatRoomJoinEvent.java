package kr.co.pennyway.infra.common.event;

public record ChatRoomJoinEvent(
        Long chatRoomId,
        String userName
) {
    public static ChatRoomJoinEvent of(Long chatRoomId, String userName) {
        return new ChatRoomJoinEvent(chatRoomId, userName);
    }
}
