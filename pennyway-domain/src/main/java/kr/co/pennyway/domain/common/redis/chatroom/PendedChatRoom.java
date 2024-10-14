package kr.co.pennyway.domain.common.redis.chatroom;

import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Getter
@RedisHash(value = "pendedChatRoom", timeToLive = 5 * 60)
public class PendedChatRoom {
    @Id
    private final Long userId;
    private final Long chatRoomId;
    private final String title;
    private final String description;
    private final Integer password;

    private PendedChatRoom(Long chatRoomId, Long userId, String title, String description, Integer password) {
        validate(chatRoomId, userId, title, description, password);
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.password = password;
    }

    public static PendedChatRoom of(Long chatRoomId, Long userId, String title, String description, Integer password) {
        return new PendedChatRoom(chatRoomId, userId, title, description, password);
    }

    private void validate(Long id, Long userId, String title, String description, Integer password) {
        Objects.requireNonNull(id, "채팅방 ID는 null일 수 없습니다.");
        Objects.requireNonNull(userId, "사용자 ID는 null일 수 없습니다.");

        if (!StringUtils.hasText(title) || title.length() > 50) {
            throw new IllegalArgumentException("제목은 null이거나 빈 문자열이 될 수 없으며, 50자 이하로 제한됩니다.");
        }

        if (description != null && description.length() > 100) {
            throw new IllegalArgumentException("설명은 null이거나 빈 문자열이 될 수 있으며, 100자 이하로 제한됩니다.");
        }

        if (password != null && password < 0 && password.toString().length() != 6) {
            throw new IllegalArgumentException("비밀번호는 null이거나, 6자리 정수여야 하며, 음수는 허용하지 않습니다.");
        }
    }

    public ChatRoom toChatRoom(String backgroundImageUrl) {
        return ChatRoom.builder()
                .id(chatRoomId)
                .title(title)
                .description(description)
                .backgroundImageUrl(backgroundImageUrl)
                .password(password)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PendedChatRoom that)) return false;
        return userId.equals(that.userId) && chatRoomId.equals(that.chatRoomId);
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = ((1 << 5) - 1) * result + chatRoomId.hashCode();
        return result;
    }
}
