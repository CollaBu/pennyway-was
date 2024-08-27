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

    @ColumnDefault("true")
    private Boolean activated;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime lastSignedInAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private DeviceToken(String token, Boolean activated, User user) {
        this.token = Objects.requireNonNull(token, "token은 null이 될 수 없습니다.");
        this.activated = Objects.requireNonNull(activated, "activated는 null이 될 수 없습니다.");
        this.user = Objects.requireNonNull(user, "user는 null이 될 수 없습니다.");
        this.lastSignedInAt = LocalDateTime.now();
    }

    public static DeviceToken of(String token, User user) {
        return new DeviceToken(token, Boolean.TRUE, user);
    }

    public Boolean isActivated() {
        return activated;
    }

    public void activate() {
        this.activated = Boolean.TRUE;
    }

    public void deactivate() {
        this.activated = Boolean.FALSE;
    }

    public void updateLastSignedInAt() {
        this.lastSignedInAt = LocalDateTime.now();
    }

    /**
     * 디바이스 토큰이 만료되었는지 확인한다.
     *
     * @return 토큰이 갱신된지 7일이 지났으면 true, 그렇지 않으면 false
     */
    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();

        return lastSignedInAt.plusDays(7).isBefore(now);
    }

    @Override
    public String toString() {
        return "DeviceToken {" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", activated=" + activated + '}';
    }
}
