package kr.co.pennyway.domain.common.redis.chatroom;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "pendedChatRoom", timeToLive = 5)
public class PendedChatRoom {
    @Id
    private final Long id;

    private String title;
    private String description;
    private Integer password;

    @Builder
    private PendedChatRoom(Long id, String title, String description, Integer password) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.password = password;
    }

    public static PendedChatRoom of(Long id, String title, String description, Integer password) {
        return new PendedChatRoom(id, title, description, password);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PendedChatRoom that)) return false;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
