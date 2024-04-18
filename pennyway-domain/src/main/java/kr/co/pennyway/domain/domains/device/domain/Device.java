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
@Table(name = "device")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Device extends DateAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private String model;
    private String os;
    @ColumnDefault("true")
    private Boolean activated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Device(String token, String model, String os, Boolean activated, User user) {
        this.token = token;
        this.model = model;
        this.os = os;
        this.activated = activated;
        this.user = user;
    }

    public static Device of(String token, String model, String os, User user) {
        return new Device(token, model, os, Boolean.TRUE, user);
    }

    public void updateToken(String token) {
        this.token = token;
    }

    public Boolean isActivated() {
        return activated;
    }

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", model='" + model + '\'' +
                ", os='" + os + '}';
    }
}
