package kr.co.pennyway.socket.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.co.pennyway.domain.common.redis.message.type.MessageContentType;

public record ChatMessageDto(
        @NotNull(message = "메시지 내용은 null을 허용하지 않습니다.")
        @Size(min = 1, max = 1000, message = "메시지 내용은 1자 이상 1000자 이하로 입력해주세요.")
        String content,
        @NotNull(message = "메시지 타입은 null을 허용하지 않습니다.")
        MessageContentType contentType
) {
}
