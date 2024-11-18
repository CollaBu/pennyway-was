package kr.co.pennyway.domain.common.redis.refresh;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("refreshToken")
@Getter
@ToString(of = {"userId", "token", "ttl"})
@EqualsAndHashCode(of = {"userId", "token"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    private String id;
    private Long userId;
    private String deviceId;
    private long ttl;
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
