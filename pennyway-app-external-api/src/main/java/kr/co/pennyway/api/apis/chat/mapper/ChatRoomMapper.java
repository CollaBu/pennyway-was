package kr.co.pennyway.api.apis.chat.mapper;

import kr.co.pennyway.api.apis.chat.dto.ChatMemberRes;
import kr.co.pennyway.api.apis.chat.dto.ChatRes;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.common.response.SliceResponseTemplate;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import kr.co.pennyway.domain.domains.member.dto.ChatMemberResult;
import kr.co.pennyway.domain.domains.message.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.ArrayList;
import java.util.List;

@Mapper
public final class ChatRoomMapper {
    /**
     * 채팅방 상세 정보를 SliceResponseTemplate 형태로 변환한다.
     * 해당 메서드는 언제나 채팅방 검색 응답으로 사용되며, 마지막 메시지 정보는 null로 설정된다.
     *
     * @param details
     * @param pageable
     * @return
     */
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
                            null,
                            0
                    )
            );
        }

        return SliceResponseTemplate.of(contents, pageable, contents.size(), details.hasNext());
    }

    public static List<ChatRoomRes.Detail> toChatRoomResDetails(List<ChatRoomRes.Info> details) {
        List<ChatRoomRes.Detail> responses = new ArrayList<>();

        for (ChatRoomRes.Info info : details) {
            responses.add(
                    new ChatRoomRes.Detail(
                            info.chatRoom().id(),
                            info.chatRoom().title(),
                            info.chatRoom().description(),
                            info.chatRoom().backgroundImageUrl(),
                            info.chatRoom().password() != null,
                            info.chatRoom().isAdmin(),
                            info.chatRoom().participantCount(),
                            info.chatRoom().createdAt(),
                            info.lastMessage(),
                            info.unreadMessageCount()
                    )
            );
        }

        return responses;
    }

    public static List<ChatRoomRes.Detailv2> toChatRoomResDetailsV2(List<ChatRoomRes.Info> details) {
        List<ChatRoomRes.Detailv2> responses = new ArrayList<>();

        for (ChatRoomRes.Info info : details) {
            responses.add(
                    new ChatRoomRes.Detailv2(
                            info.chatRoom().id(),
                            info.chatRoom().title(),
                            info.chatRoom().description(),
                            info.chatRoom().backgroundImageUrl(),
                            info.chatRoom().isNotifyEnabled(),
                            info.chatRoom().password() != null,
                            info.chatRoom().isAdmin(),
                            info.chatRoom().participantCount(),
                            info.chatRoom().createdAt(),
                            info.lastMessage(),
                            info.unreadMessageCount()
                    )
            );
        }

        return responses;
    }

    public static ChatRoomRes.Detail toChatRoomResDetail(ChatRoom chatRoom, ChatRes.ChatDetail lastMessage, boolean isAdmin, int participantCount, long unreadMessageCount) {
        return ChatRoomRes.Detail.of(chatRoom, lastMessage, isAdmin, participantCount, unreadMessageCount);
    }

    public static ChatRoomRes.RoomWithParticipants toChatRoomResRoomWithParticipants(ChatMemberResult.Detail myInfo, List<ChatMemberResult.Detail> recentParticipants, List<ChatMemberResult.Summary> otherParticipants, List<ChatMessage> chatMessages) {
        List<ChatMemberRes.MemberDetail> recentParticipantsRes = recentParticipants.stream()
                .map(participant -> ChatMemberRes.MemberDetail.from(participant, false))
                .toList();
        List<ChatMemberRes.MemberSummary> otherParticipantsRes = otherParticipants.stream()
                .map(ChatMemberRes.MemberSummary::from)
                .toList();

        List<ChatRes.ChatDetail> chatMessagesRes = chatMessages.stream()
                .map(ChatRes.ChatDetail::from)
                .toList();

        return ChatRoomRes.RoomWithParticipants.builder()
                .myInfo(ChatMemberRes.MemberDetail.from(myInfo, true))
                .recentParticipants(recentParticipantsRes)
                .otherParticipants(otherParticipantsRes)
                .recentMessages(chatMessagesRes)
                .build();
    }

    public static ChatRoomRes.AdminView toChatRoomResAdminView(ChatRoom chatRoom) {
        return ChatRoomRes.AdminView.of(chatRoom);
    }
}
