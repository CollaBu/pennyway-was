package kr.co.pennyway.domain.common.redis.message.domain;

import jakarta.persistence.Convert;
import kr.co.pennyway.domain.common.converter.MessageCategoryTypeConverter;
import kr.co.pennyway.domain.common.converter.MessageContentTypeConverter;
import kr.co.pennyway.domain.common.redis.message.type.MessageCategoryType;
import kr.co.pennyway.domain.common.redis.message.type.MessageContentType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

/**
 * 채팅 메시지를 표현하는 클래스입니다.
 * Redis에 저장되는 채팅 메시지의 기본 단위입니다.
 */
@Getter
@RedisHash(value = "chatroom")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
    private Long chatRoomId;
    private Long chatId;
    private String content;
    @Convert(converter = MessageContentTypeConverter.class)
    private MessageContentType contentType;
    @Convert(converter = MessageCategoryTypeConverter.class)
    private MessageCategoryType categoryType;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private Long sender;

    protected ChatMessage(ChatMessageBuilder builder) {
        this.chatRoomId = builder.getChatRoomId();
        this.chatId = builder.getChatId();
        this.content = builder.getContent();
        this.contentType = builder.getContentType();
        this.categoryType = builder.getCategoryType();
        this.createdAt = LocalDateTime.now();
        this.deletedAt = null;
        this.sender = builder.getSender();
    }


    @Override
    public String toString() {
        return "ChatMessage{" +
                "chatRoomId='" + chatRoomId + '\'' +
                ", chatId='" + chatId + '\'' +
                ", content='" + content + '\'' +
                ", contentType='" + contentType + '\'' +
                ", categoryType='" + categoryType + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", deletedAt='" + deletedAt + '\'' +
                ", sender='" + sender + '\'' +
                '}';
    }
}
