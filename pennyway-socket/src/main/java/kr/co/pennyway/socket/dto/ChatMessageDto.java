package kr.co.pennyway.socket.dto;

import kr.co.pennyway.domain.common.redis.message.type.MessageContentType;

public record ChatMessageDto(
        String content,
        MessageContentType contentType
) {
}
