package kr.co.pennyway.domain.domains.device.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.model.DateAuditable;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Table(name = "device_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken extends DateAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    @ColumnDefault("true")
    private Boolean activated;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private User user;

    private DeviceToken(String token, Boolean activated, User user) {
        this.token = token;
        this.activated = activated;
        this.user = user;
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

    /**
     * 디바이스 토큰을 갱신하고 활성화 상태로 변경한다.
     */
    public void updateToken(String token) {
        this.activated = Boolean.TRUE;
        this.token = token;
    }

    @Override
    public String toString() {
        return "DeviceToken {" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", activated=" + activated + '}';
    }
}
