package kr.co.pennyway.domain.domains.chatstatus.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Table(name = "chat_message_status")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long chatRoomId;
    private Long lastReadMessageId;
    private LocalDateTime updatedAt;

    public ChatMessageStatus(Long userId, Long chatRoomId, Long lastReadMessageId) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.chatRoomId = Objects.requireNonNull(chatRoomId, "chatRoomId must not be null");
        this.lastReadMessageId = Objects.requireNonNull(lastReadMessageId, "lastReadMessageId must not be null");
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLastReadMessageId(Long messageId) {
        if (this.lastReadMessageId == null || messageId > this.lastReadMessageId) {
            this.lastReadMessageId = messageId;
        }
    }
}
