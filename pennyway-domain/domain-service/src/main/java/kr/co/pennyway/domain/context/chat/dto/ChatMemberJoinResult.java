package kr.co.pennyway.domain.context.chat.dto;

import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;

public record ChatMemberJoinResult(
        ChatRoom chatRoom,
        Long currentMemberCount
) {
    public static ChatMemberJoinResult of(ChatRoom chatRoom, Long currentMemberCount) {
        return new ChatMemberJoinResult(chatRoom, currentMemberCount);
    }
}
