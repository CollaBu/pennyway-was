package kr.co.pennyway.api.apis.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;

public final class ChatRoomReq {
    @Schema(title = "채팅방 생성 요청")
    public record Create(
            @NotBlank
            @Size(min = 1, max = 50)
            @Schema(description = "채팅방 제목. NULL 혹은 공백은 허용하지 않으며, 1~50자 이내의 문자열이어야 한다.", example = "페니웨이")
            String title,
            @Size(min = 1, max = 100)
            @Schema(description = "채팅방 설명. NULL을 허용하며, 문자가 존재할 시 공백 허용 없이 1~100자 이내의 문자열이어야 한다.", example = "페니웨이 채팅방입니다.")
            String description,
            @Schema(description = "채팅방 배경 이미지 URL. NULL을 허용한다.", example = "https://pennyway.co.kr/background.jpg")
            String backgroundImageUrl,
            @Schema(description = "채팅방 비밀번호. NULL을 허용한다. 비밀번호는 6자리 정수만 허용", example = "123456")
            @Size(min = 6, max = 6)
            Integer password
    ) {
        public ChatRoom toEntity() {
            return ChatRoom.builder()
                    .title(title)
                    .description(description)
                    .backgroundImageUrl(backgroundImageUrl)
                    .password(password)
                    .build();
        }
    }
}
