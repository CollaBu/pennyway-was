package kr.co.pennyway.domain.context.chat.dto;

import org.springframework.util.StringUtils;

public record ChatRoomPatchCommand(
        Long chatRoomId,
        String title,
        String description,
        String backgroundImageUrl,
        Integer password
) {
    public ChatRoomPatchCommand {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("채팅방 ID는 NULL이 될 수 없습니다.");
        }

        if (!StringUtils.hasText(title) && title.length() >= 50) {
            throw new IllegalArgumentException("채팅방 제목은 NULL 혹은 공백을 허용하지 않으며, 1~50자 이내의 문자열이어야 합니다.");
        }

        if (description != null && description.length() >= 100) {
            throw new IllegalArgumentException("채팅방 설명은 NULL 혹은, 문자가 존재할 시 공백 허용 없이 1~100자 이내의 문자열이어야 합니다.");
        }

        if (password != null && password.toString().length() != 6) {
            throw new IllegalArgumentException("채팅방 비밀번호는 Null 혹은, 6자리 정수여야 합니다.");
        }

        if (backgroundImageUrl != null && backgroundImageUrl.startsWith("chatroom/")) {
            throw new IllegalArgumentException("채팅방 배경 이미지 URL은 'chatroom/' 으로 시작해야 합니다.");
        }
    }
}
