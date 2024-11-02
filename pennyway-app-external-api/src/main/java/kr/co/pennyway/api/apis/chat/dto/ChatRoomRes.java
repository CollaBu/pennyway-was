package kr.co.pennyway.api.apis.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ChatRoomRes {
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
            LocalDateTime createdAt
    ) {
        public Detail(Long id, String title, String description, String backgroundImageUrl, boolean isPrivate, boolean isAdmin, int participantCount, LocalDateTime createdAt) {
            this.id = id;
            this.title = title;
            this.description = Objects.toString(description, "");
            this.backgroundImageUrl = Objects.toString(backgroundImageUrl, "");
            this.isPrivate = isPrivate;
            this.isAdmin = isAdmin;
            this.participantCount = participantCount;
            this.createdAt = createdAt;
        }

        public static Detail from(ChatRoom chatRoom, boolean isAdmin, int participantCount) {
            return new Detail(
                    chatRoom.getId(),
                    chatRoom.getTitle(),
                    chatRoom.getDescription(),
                    chatRoom.getBackgroundImageUrl(),
                    chatRoom.getPassword() != null,
                    isAdmin,
                    participantCount,
                    chatRoom.getCreatedAt()
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
            ChatMemberRes.Detail myInfo,
            @Schema(description = "최근에 채팅 메시지를 보낸 참여자의 상세 정보 목록")
            List<ChatMemberRes.Detail> recentParticipants,
            @Schema(description = "채팅방에서 내 정보와 최근 활동자를 제외한 참여자 ID 목록")
            List<Long> otherParticipantIds,
            @Schema(description = "최근 채팅 이력")
            List<ChatRes.Detail> recentMessages
    ) {

    }
}
