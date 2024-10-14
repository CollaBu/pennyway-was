package kr.co.pennyway.domain.domains.chatroom.dto;

import java.time.LocalDateTime;

public record ChatRoomDetail(
        Long id,
        String title,
        String description,
        String backgroundImageUrl,
        Integer password,
        LocalDateTime createdAt,
        int participantCount
) {
}
