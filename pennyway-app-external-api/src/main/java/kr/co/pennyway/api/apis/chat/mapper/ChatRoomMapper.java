package kr.co.pennyway.api.apis.chat.mapper;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;

@Mapper
public final class ChatRoomMapper {
    public static ChatRoomRes.Detail toChatRoomResDetail(ChatRoom chatRoom, int participantCount) {
        return ChatRoomRes.Detail.from(chatRoom, participantCount);
    }
}
