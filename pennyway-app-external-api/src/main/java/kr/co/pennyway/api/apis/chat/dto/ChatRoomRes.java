package kr.co.pennyway.api.apis.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ChatRoomRes {
    /**
     * 채팅방 정보를 담기 위한 DTO
     *
     * @param chatRoom           {@link ChatRoomDetail} : 채팅방 정보
     * @param unreadMessageCount long : 읽지 않은 메시지 수
     * @param lastMessage        {@link ChatRes.ChatDetail} : 가장 최근 메시지. 없을 경우 null
     */
    public record Info(
            ChatRoomDetail chatRoom,
            long unreadMessageCount,
            ChatRes.ChatDetail lastMessage
    ) {
        public static Info of(ChatRoomDetail chatRoom, long unreadMessageCount, ChatRes.ChatDetail recentMessage) {
            return new Info(chatRoom, unreadMessageCount, recentMessage);
        }
    }

    @Schema(description = "채팅방 상세 정보")
    public record Detail(
            @Schema(description = "채팅방 ID", type = "long")
            Long id,
            @Schema(description = "채팅방 제목")
            String title,
            @Schema(description = "채팅방 설명")
            String description,
            @Schema(description = "채팅방 배경 이미지 URL")
            String backgroundImageUrl,
            @Schema(description = "채팅방 비공개 여부")
            boolean isPrivate,
            @Schema(description = "어드민 여부. 채팅방의 관리자라면 true, 아니라면 false")
            boolean isAdmin,
            @Schema(description = "채팅방 참여자 수")
            int participantCount,
            @Schema(description = "채팅방 개설일")
            @JsonSerialize(using = LocalDateTimeSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt,
            @Schema(description = "마지막 메시지 정보. 없을 경우 null이 반환된다.")
            ChatRes.ChatDetail lastMessage,
            @Schema(description = "읽지 않은 메시지 수. 100 이상의 값을 가지면, 100으로 표시된다.")
            long unreadMessageCount
    ) {
        public Detail(Long id, String title, String description, String backgroundImageUrl, boolean isPrivate, boolean isAdmin, int participantCount, LocalDateTime createdAt, ChatRes.ChatDetail lastMessage, long unreadMessageCount) {
            this.id = id;
            this.title = title;
            this.description = Objects.toString(description, "");
            this.backgroundImageUrl = Objects.toString(backgroundImageUrl, "");
            this.isPrivate = isPrivate;
            this.isAdmin = isAdmin;
            this.participantCount = participantCount;
            this.createdAt = createdAt;
            this.lastMessage = lastMessage;
            this.unreadMessageCount = (unreadMessageCount > 100) ? 100 : unreadMessageCount;
        }

        public static Detail of(ChatRoom chatRoom, ChatRes.ChatDetail lastMessage, boolean isAdmin, int participantCount, long unreadMessageCount) {
            return new Detail(
                    chatRoom.getId(),
                    chatRoom.getTitle(),
                    chatRoom.getDescription(),
                    chatRoom.getBackgroundImageUrl(),
                    chatRoom.getPassword() != null,
                    isAdmin,
                    participantCount,
                    chatRoom.getCreatedAt(),
                    lastMessage,
                    unreadMessageCount
            );
        }

        public static Detail from(ChatRoomRes.Info info) {
            return new Detail(
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
            );
        }
    }

    @Schema(description = "채팅방 정보 (어드민용)")
    public record AdminView(
            @Schema(description = "채팅방 ID", type = "long")
            Long id,
            @Schema(description = "채팅방 제목")
            String title,
            @Schema(description = "채팅방 설명")
            String description,
            @Schema(description = "채팅방 배경 이미지 URL")
            String backgroundImageUrl,
            @Schema(description = "비밀번호")
            Integer password
    ) {
        public static AdminView of(ChatRoom chatRoom) {
            return new AdminView(
                    chatRoom.getId(),
                    chatRoom.getTitle(),
                    chatRoom.getDescription(),
                    chatRoom.getBackgroundImageUrl(),
                    chatRoom.getPassword()
            );
        }
    }

    @Schema(description = "채팅방 요약 정보")
    public record Summary(
            @Schema(description = "채팅방 ID 목록. 빈 목록일 경우 빈 배열이 반환된다. 각 요소는 long 타입이다.")
            Set<Long> chatRoomIds
    ) {
    }

    @Schema(description = "채팅방 참여자 정보 (방의 참여자 + 최근 메시지)")
    @Builder
    public record RoomWithParticipants(
            @Schema(description = "채팅방에서 내 정보")
            ChatMemberRes.MemberDetail myInfo,
            @Schema(description = "최근에 채팅 메시지를 보낸 참여자의 상세 정보 목록. 내가 방장이 아니라면, 최근에 활동 내역이 없어도 방장 정보가 포함된다.")
            List<ChatMemberRes.MemberDetail> recentParticipants,
            @Schema(description = "채팅방에서 내 정보와 최근 활동자를 제외한 참여자 ID, Name 목록")
            List<ChatMemberRes.MemberSummary> otherParticipants,
            @Schema(description = "최근 채팅 이력. 메시지는 최신순으로 정렬되어 반환.")
            List<ChatRes.ChatDetail> recentMessages
    ) {

    }
}
