package kr.co.pennyway.api.apis.chat.mapper;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.common.response.SliceResponseTemplate;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.ArrayList;
import java.util.List;

@Mapper
public final class ChatRoomMapper {


    public static SliceResponseTemplate<ChatRoomRes.Detail> toChatRoomResDetails(Slice<ChatRoomDetail> details, Pageable pageable) {
        List<ChatRoomRes.Detail> contents = toChatRoomResDetails(details.getContent());

        return SliceResponseTemplate.of(contents, pageable, contents.size(), details.hasNext());
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
                            detail.isAdmin(),
                            detail.participantCount(),
                            detail.createdAt()
                    )
            );
        }

        return responses;
    }

    public static ChatRoomRes.Detail toChatRoomResDetail(ChatRoom chatRoom, boolean isAdmin, int participantCount) {
        return ChatRoomRes.Detail.from(chatRoom, isAdmin, participantCount);
    }
}
