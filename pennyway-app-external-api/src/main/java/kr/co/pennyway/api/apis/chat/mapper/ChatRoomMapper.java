package kr.co.pennyway.api.apis.chat.mapper;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;

import java.util.ArrayList;
import java.util.List;

@Mapper
public final class ChatRoomMapper {
    public static ChatRoomRes.Detail toChatRoomResDetail(ChatRoom chatRoom, int participantCount) {
        return ChatRoomRes.Detail.from(chatRoom, participantCount);
    }

    public static List<ChatRoomRes.Detail> toChatRoomResDetails(List<ChatRoomDetail> details) {
        List<ChatRoomRes.Detail> responses = new ArrayList<>();

        for (ChatRoomDetail detail : details) {
            responses.add(
                    new ChatRoomRes.Detail(
                            detail.id(),
                            detail.title(),
                            detail.description(),
                            detail.backgroundImageUrl(),
                            detail.password() != null,
                            detail.participantCount(),
                            detail.createdAt()
                    )
            );
        }

        return responses;
    }
}
