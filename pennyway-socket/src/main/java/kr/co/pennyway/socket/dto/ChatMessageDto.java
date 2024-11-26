package kr.co.pennyway.socket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.co.pennyway.domain.domains.message.domain.ChatMessage;
import kr.co.pennyway.domain.domains.message.type.MessageCategoryType;
import kr.co.pennyway.domain.domains.message.type.MessageContentType;

import java.time.LocalDateTime;

public final class ChatMessageDto {
    public record Request(
            @NotNull(message = "메시지 내용은 null을 허용하지 않습니다.")
            @Size(min = 1, max = 1000, message = "메시지 내용은 1자 이상 1000자 이하로 입력해주세요.")
            String content,
            @NotNull(message = "메시지 타입은 null을 허용하지 않습니다.")
            MessageContentType contentType
    ) {
    }

    public record Response(
            Long chatRoomId,
            Long chatId,
            String content,
            MessageContentType contentType,
            MessageCategoryType categoryType,
            @JsonSerialize(using = LocalDateTimeSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt,
            Long senderId
    ) {
        public static Response from(ChatMessage message) {
            return new Response(
                    message.getChatRoomId(),
                    message.getChatId(),
                    message.getContent(),
                    message.getContentType(),
                    message.getCategoryType(),
                    message.getCreatedAt(),
                    message.getSender()
            );
        }
    }
}
