package kr.co.pennyway.domain.common.redis.sign;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Getter
@RedisHash(value = "signEventLog", timeToLive = 60 * 60 * 24)
public class SignEventLog {
    @Id
    private final Long userId;
    private final LocalDateTime signedAt;
    private final String ipAddress;
    private final String deviceModel;
    private final String os;

    @Builder
    private SignEventLog(Long userId, LocalDateTime signedAt, String ipAddress, String deviceModel, String os) {
        this.userId = userId;
        this.signedAt = signedAt;
        this.ipAddress = ipAddress;
        this.deviceModel = deviceModel;
        this.os = os;
    }

    @Override
    public String toString() {
        return "SignEventLog{" +
                "userId=" + userId +
                ", signedAt=" + signedAt +
                ", ipAddress='" + ipAddress + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", os='" + os + '\'' +
                '}';
    }
}
