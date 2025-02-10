package kr.co.pennyway.domain.domains.forbidden.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@RedisHash("forbiddenToken")
public class ForbiddenToken {
    @Id
    private final String accessToken;
    private final Long userId;
    @TimeToLive
    private final long ttl;

    @Builder
    private ForbiddenToken(String accessToken, Long userId, long ttl) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.ttl = ttl;
    }

    public static ForbiddenToken of(String accessToken, Long userId, long ttl) {
        return new ForbiddenToken(accessToken, userId, ttl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForbiddenToken that)) return false;
        return accessToken.equals(that.accessToken) && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        int result = accessToken.hashCode();
        result = ((1 << 5) - 1) * result + userId.hashCode();
        return result;
    }
}