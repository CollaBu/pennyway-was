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
    private final String id;
    private final Long userId;
    private final String deviceId;
    private final long ttl;
    private String token;

    @Builder
    private RefreshToken(Long userId, String deviceId, String token, long ttl) {
        this.id = createId(userId, deviceId);
        this.userId = userId;
        this.deviceId = deviceId;
        this.token = token;
        this.ttl = ttl;
    }

    public static RefreshToken of(Long userId, String deviceId, String token, long ttl) {
        return RefreshToken.builder()
                .userId(userId)
                .deviceId(deviceId)
                .token(token)
                .ttl(ttl)
                .build();
    }

    public static String createId(Long userId, String deviceId) {
        return userId + ":" + deviceId;
    }

    protected void rotation(String token) {
        this.token = token;
    }
}
