package kr.co.pennyway.domain.context.chat.dto;

public record ChatMemberJoinResult(
        Long chatRoomId,
        Long currentMemberCount
) {
    public static ChatMemberJoinResult of(Long chatRoomId, Long currentMemberCount) {
        return new ChatMemberJoinResult(chatRoomId, currentMemberCount);
    }
}
