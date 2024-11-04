package kr.co.pennyway.api.apis.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessage;
import kr.co.pennyway.domain.common.redis.message.type.MessageCategoryType;
import kr.co.pennyway.domain.common.redis.message.type.MessageContentType;

import java.time.LocalDateTime;

public final class ChatRes {
    @Schema(description = "채팅 메시지 상세 정보")
    public record ChatDetail(
            @Schema(description = "채팅방 ID", type = "long")
            Long chatRoomId,
            @Schema(description = "채팅 ID", type = "long")
            Long chatId,
            @Schema(description = "채팅 내용")
            String content,
            @Schema(description = "채팅 내용 타입")
            MessageContentType contentType,
            @Schema(description = "채팅 메시지 카테고리 타입")
            MessageCategoryType categoryType,
            @Schema(description = "채팅 생성일")
            @JsonSerialize(using = LocalDateTimeSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt,
            @Schema(description = "채팅 보낸 사람 ID", type = "long")
            Long senderId
    ) {
        public static ChatDetail from(ChatMessage message) {
            return new ChatDetail(
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
