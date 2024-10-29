package kr.co.pennyway.infra.common.event;

public record ChatRoomJoinEvent(
        Long chatRoomId,
        String userName
) {
}
