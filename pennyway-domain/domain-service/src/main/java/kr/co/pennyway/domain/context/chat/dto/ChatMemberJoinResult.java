package kr.co.pennyway.domain.context.chat.dto;

import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;

public record ChatMemberJoinResult(
        ChatRoom chatRoom,
        String memberName,
        Long currentMemberCount
) {
    public ChatMemberJoinResult(ChatRoom chatRoom, String memberName, Long currentMemberCount) {
        this.chatRoom = chatRoom;
        this.memberName = memberName;
        this.currentMemberCount = currentMemberCount + 1;
    }

    public static ChatMemberJoinResult of(ChatRoom chatRoom, String memberName, Long currentMemberCount) {
        return new ChatMemberJoinResult(chatRoom, memberName, currentMemberCount);
    }
}
