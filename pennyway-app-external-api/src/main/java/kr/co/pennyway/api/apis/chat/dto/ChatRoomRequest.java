package kr.co.pennyway.api.apis.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;

public final class ChatRoomRequest {
    public record Create(
            @NotBlank
            @Size(min = 1, max = 50)
            String title,
            @Size(min = 1, max = 100)
            String description,
            String backgroundImageUrl,
            Integer password
    ) {
        public static ChatRoom toEntity(Create request) {
            return ChatRoom.builder()
                    .title(request.title())
                    .description(request.description())
                    .backgroundImageUrl(request.backgroundImageUrl())
                    .password(request.password())
                    .build();
        }
    }
}
