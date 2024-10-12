package kr.co.pennyway.api.apis.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;

import java.time.LocalDateTime;
import java.util.Objects;

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
            @JsonSerialize(using = LocalDateTimeSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt
    ) {
        public static Detail from(ChatRoom chatRoom, int participantCount) {
            return new Detail(
                    chatRoom.getId(),
                    chatRoom.getTitle(),
                    Objects.toString(chatRoom.getDescription(), ""),
                    Objects.toString(chatRoom.getBackgroundImageUrl(), ""),
                    chatRoom.getPassword() != null,
                    participantCount,
                    chatRoom.getCreatedAt()
            );
        }
    }
}
