package kr.co.pennyway.domain.common.redis.message.domain;

import jakarta.persistence.Convert;
import jakarta.persistence.Id;
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
    /**
     * 채팅 메시지 ID는 "chatroom:{roomId}:message:{messageId}" 형태로 생성한다.
     */
    @Id
    private String id;
    private String content;
    @Convert(converter = MessageContentTypeConverter.class)
    private MessageContentType contentType;
    @Convert(converter = MessageCategoryTypeConverter.class)
    private MessageCategoryType categoryType;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private Long sender;

    protected ChatMessage(ChatMessageBuilder builder) {
        this.id = builder.getChatRoomId() + ":message:" + builder.getChatId();
        this.content = builder.getContent();
        this.contentType = builder.getContentType();
        this.categoryType = builder.getCategoryType();
        this.createdAt = LocalDateTime.now();
        this.deletedAt = null;
        this.sender = builder.getSender();
    }

    public Long getChatRoomId() {
        return Long.parseLong(id.split(":")[0]);
    }

    public Long getChatId() {
        return Long.parseLong(id.split(":")[2]);
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", contentType=" + contentType +
                ", categoryType=" + categoryType +
                ", createdAt=" + createdAt +
                ", deletedAt=" + deletedAt +
                ", sender=" + sender +
                '}';
    }
}
