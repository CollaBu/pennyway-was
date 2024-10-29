package kr.co.pennyway.api.apis.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

public final class ChatMemberReq {
    @Schema(title = "채팅방 멤버 가입 요청 DTO")
    public static class Join {
        @Schema(description = "채팅방 비밀번호. NULL을 허용한다. 비밀번호는 6자리 정수만 허용", example = "123456")
        @Pattern(regexp = "^[0-9]{6}$", message = "채팅방 비밀번호는 6자리 정수여야 합니다.")
        private String password;

        // 메서드 표현 일관성을 유지하고, password를 Integer로 변환하여 반환하는 getter
        public Integer password() {
            return password != null ? Integer.valueOf(password) : null;
        }

        // Swagger UI에서 표현하기 위한 getter
        public String getPassword() {
            return password;
        }
    }
}
