package kr.co.pennyway.domain.common.redis.refresh;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("refreshToken")
@Getter
@ToString(of = {"userId", "token", "ttl"})
@EqualsAndHashCode(of = {"userId", "token"})
public class RefreshToken {
    @Id
    private final Long userId;
    private final long ttl;
    private String token;

    @Builder
    private RefreshToken(String token, Long userId, long ttl) {
        this.token = token;
        this.userId = userId;
        this.ttl = ttl;
    }

    public static RefreshToken of(Long userId, String token, long ttl) {
        return RefreshToken.builder()
                .userId(userId)
                .token(token)
                .ttl(ttl)
                .build();
    }

    protected void rotation(String token) {
        this.token = token;
    }
}
