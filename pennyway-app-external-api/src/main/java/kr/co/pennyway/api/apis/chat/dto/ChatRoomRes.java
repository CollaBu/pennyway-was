package kr.co.pennyway.api.apis.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;

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
            @Schema(description = "채팅방 참여자 수")
            int participantCount,
            @Schema(description = "채팅방 개설일")
            String createdAt
    ) {
        public static Detail from(ChatRoom chatRoom, int participantCount) {
            return new Detail(
                    chatRoom.getId(),
                    chatRoom.getTitle(),
                    chatRoom.getDescription(),
                    chatRoom.getBackgroundImageUrl(),
                    chatRoom.getPassword() != null,
                    participantCount,
                    chatRoom.getCreatedAt().toString()
            );
        }
    }
}
