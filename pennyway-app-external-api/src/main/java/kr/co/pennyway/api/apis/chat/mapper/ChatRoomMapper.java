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
    public static SliceResponseTemplate<ChatRoomRes.Detail> toChatRoomResDetails(Slice<ChatRoomDetail> details, Pageable pageable, String objectPrefix) {
        List<ChatRoomRes.Detail> contents = new ArrayList<>();
        for (ChatRoomDetail detail : details.getContent()) {
            contents.add(
                    new ChatRoomRes.Detail(
                            detail.id(),
                            detail.title(),
                            detail.description(),
                            createBackGroundImageUrl(detail.backgroundImageUrl(), objectPrefix),
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

    public static List<ChatRoomRes.Detail> toChatRoomResDetails(List<ChatRoomRes.Info> details, String objectPrefix) {
        List<ChatRoomRes.Detail> responses = new ArrayList<>();

        for (ChatRoomRes.Info info : details) {
            responses.add(
                    new ChatRoomRes.Detail(
                            info.chatRoom().id(),
                            info.chatRoom().title(),
                            info.chatRoom().description(),
                            createBackGroundImageUrl(info.chatRoom().backgroundImageUrl(), objectPrefix),
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

    public static List<ChatRoomRes.Detailv2> toChatRoomResDetailsV2(List<ChatRoomRes.Info> details, String objectPrefix) {
        List<ChatRoomRes.Detailv2> responses = new ArrayList<>();

        for (ChatRoomRes.Info info : details) {
            responses.add(
                    new ChatRoomRes.Detailv2(
                            info.chatRoom().id(),
                            info.chatRoom().title(),
                            info.chatRoom().description(),
                            createBackGroundImageUrl(info.chatRoom().backgroundImageUrl(), objectPrefix),
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

    public static ChatRoomRes.Detail toChatRoomResDetail(ChatRoom chatRoom, ChatRes.ChatDetail lastMessage, boolean isAdmin, int participantCount, long unreadMessageCount, String objectPrefix) {
        return new ChatRoomRes.Detail(
                chatRoom.getId(),
                chatRoom.getTitle(),
                chatRoom.getDescription(),
                createBackGroundImageUrl(chatRoom.getBackgroundImageUrl(), objectPrefix),
                chatRoom.getPassword() != null,
                isAdmin,
                participantCount,
                chatRoom.getCreatedAt(),
                lastMessage,
                unreadMessageCount
        );
    }

    public static ChatRoomRes.RoomWithParticipants toChatRoomResRoomWithParticipants(ChatMemberResult.Detail myInfo, List<ChatMemberResult.Detail> recentParticipants, List<ChatMemberResult.Summary> otherParticipants, List<ChatMessage> chatMessages, String objectPrefix) {
        List<ChatMemberRes.MemberDetail> recentParticipantsRes = recentParticipants.stream()
                .map(participant -> createMemberDetail(participant, false, objectPrefix))
                .toList();
        List<ChatMemberRes.MemberSummary> otherParticipantsRes = otherParticipants.stream()
                .map(ChatMemberRes.MemberSummary::from)
                .toList();

        List<ChatRes.ChatDetail> chatMessagesRes = chatMessages.stream()
                .map(ChatRes.ChatDetail::from)
                .toList();

        return ChatRoomRes.RoomWithParticipants.builder()
                .myInfo(createMemberDetail(myInfo, true, objectPrefix))
                .recentParticipants(recentParticipantsRes)
                .otherParticipants(otherParticipantsRes)
                .recentMessages(chatMessagesRes)
                .build();
    }

    public static ChatRoomRes.AdminView toChatRoomResAdminView(ChatRoom chatRoom) {
        return ChatRoomRes.AdminView.of(chatRoom);
    }

    private static String createBackGroundImageUrl(String chatRoomBackgroundImage, String objectPrefix) {
        return (chatRoomBackgroundImage == null) ? "" : objectPrefix + chatRoomBackgroundImage;
    }

    private static ChatMemberRes.MemberDetail createMemberDetail(ChatMemberResult.Detail chatMember, boolean isMe, String objectPrefix) {
        return new ChatMemberRes.MemberDetail(
                chatMember.id(),
                chatMember.userId(),
                chatMember.name(),
                chatMember.role(),
                isMe ? chatMember.notifyEnabled() : null,
                chatMember.createdAt(),
                chatMember.profileImageUrl() == null ? "" : objectPrefix + chatMember.profileImageUrl()
        );
    }
}
