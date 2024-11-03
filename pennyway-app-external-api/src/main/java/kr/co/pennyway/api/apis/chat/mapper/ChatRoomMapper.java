package kr.co.pennyway.api.apis.chat.mapper;

import kr.co.pennyway.api.apis.chat.dto.ChatMemberRes;
import kr.co.pennyway.api.apis.chat.dto.ChatRes;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.common.response.SliceResponseTemplate;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessage;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public final class ChatRoomMapper {
    public static SliceResponseTemplate<ChatRoomRes.Detail> toChatRoomResDetails(Slice<ChatRoomDetail> details, Pageable pageable) {
        List<ChatRoomRes.Detail> contents = new ArrayList<>();
        for (ChatRoomDetail detail : details.getContent()) {
            contents.add(
                    new ChatRoomRes.Detail(
                            detail.id(),
                            detail.title(),
                            detail.description(),
                            detail.backgroundImageUrl(),
                            detail.password() != null,
                            detail.isAdmin(),
                            detail.participantCount(),
                            detail.createdAt(),
                            0
                    )
            );
        }

        return SliceResponseTemplate.of(contents, pageable, contents.size(), details.hasNext());
    }

    public static List<ChatRoomRes.Detail> toChatRoomResDetails(Map<ChatRoomDetail, Long> details) {
        List<ChatRoomRes.Detail> responses = new ArrayList<>();

        for (Map.Entry<ChatRoomDetail, Long> entry : details.entrySet()) {
            ChatRoomDetail detail = entry.getKey();
            responses.add(
                    new ChatRoomRes.Detail(
                            detail.id(),
                            detail.title(),
                            detail.description(),
                            detail.backgroundImageUrl(),
                            detail.password() != null,
                            detail.isAdmin(),
                            detail.participantCount(),
                            detail.createdAt(),
                            entry.getValue()
                    )
            );
        }

        return responses;
    }

    public static ChatRoomRes.Detail toChatRoomResDetail(ChatRoom chatRoom, boolean isAdmin, int participantCount, long unreadMessageCount) {
        return ChatRoomRes.Detail.of(chatRoom, isAdmin, participantCount, unreadMessageCount);
    }

    public static ChatRoomRes.RoomWithParticipants toChatRoomResRoomWithParticipants(ChatMember myInfo, List<ChatMember> recentParticipants, List<Long> otherMemberIds, List<ChatMessage> chatMessages) {
        List<ChatMemberRes.Detail> recentParticipantsRes = recentParticipants.stream()
                .map(participant -> ChatMemberRes.Detail.from(participant, false))
                .toList();

        List<ChatRes.Detail> chatMessagesRes = chatMessages.stream()
                .map(ChatRes.Detail::from)
                .toList();

        return ChatRoomRes.RoomWithParticipants.builder()
                .myInfo(ChatMemberRes.Detail.from(myInfo, true))
                .recentParticipants(recentParticipantsRes)
                .otherParticipantIds(otherMemberIds)
                .recentMessages(chatMessagesRes)
                .build();
    }
}
