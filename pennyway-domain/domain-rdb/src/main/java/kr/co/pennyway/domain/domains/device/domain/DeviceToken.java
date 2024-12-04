package kr.co.pennyway.domain.domains.device.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Table(name = "device_token")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private String deviceId;
    private String deviceName;

    @ColumnDefault("true")
    private Boolean activated;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime lastSignedInAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private DeviceToken(String token, String deviceId, String deviceName, Boolean activated, User user) {
        this.token = Objects.requireNonNull(token, "token은 null이 될 수 없습니다.");
        this.deviceId = Objects.requireNonNull(deviceId, "deviceId는 null이 될 수 없습니다.");
        this.deviceName = Objects.requireNonNull(deviceName, "deviceName은 null이 될 수 없습니다.");
        this.activated = Objects.requireNonNull(activated, "activated는 null이 될 수 없습니다.");
        this.user = Objects.requireNonNull(user, "user는 null이 될 수 없습니다.");
        this.lastSignedInAt = LocalDateTime.now();
    }

    public static DeviceToken of(String token, String deviceId, String deviceName, User user) {
        return new DeviceToken(token, deviceId, deviceName, Boolean.TRUE, user);
    }

    /**
     * 디바이스 토큰이 활성화되었는지 확인한다.
     *
     * @return 토큰이 활성화 되었고, 마지막 로그인 시간이 7일 이내이면 true, 그렇지 않으면 false
     */
    public Boolean isActivated() {
        LocalDateTime now = LocalDateTime.now();

        return activated && lastSignedInAt.plusDays(7).isAfter(now);
    }

    /**
     * 디바이스 토큰이 만료되었는지 확인한다.
     *
     * @return 토큰이 갱신된지 7일이 지났거나 토큰이 비활성화 되었다면 true, 그렇지 않으면 false
     */
    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();

        return !activated || lastSignedInAt.plusDays(7).isBefore(now);
    }

    public void activate() {
        lastSignedInAt = LocalDateTime.now();
        this.activated = Boolean.TRUE;
    }

    public void deactivate() {
        this.activated = Boolean.FALSE;
    }

    public void updateLastSignedInAt() {
        this.lastSignedInAt = LocalDateTime.now();
    }

    /**
     * 토큰의 소유자를 확인하고 필요한 상태 변경을 수행합니다.
     * 다른 소유자인 경우 소유자를 갱신하고, 같은 소유자인 경우 활성화만 수행합니다.
     */
    public void handleOwner(User newUser, String newDeviceId) {
        Objects.requireNonNull(newUser, "user는 null이 될 수 없습니다.");
        Objects.requireNonNull(newDeviceId, "deviceId는 null이 될 수 없습니다.");

        if (!this.user.equals(newUser)) {
            this.user = newUser;
        }

        if (!this.deviceId.equals(newDeviceId)) {
            this.deviceId = newDeviceId;
        }

        this.activate();
    }

    @Override
    public String toString() {
        return "DeviceToken {" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", activated=" + activated + '}';
    }
}
