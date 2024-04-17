package kr.co.pennyway.domain.domains.device.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.model.DateAuditable;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Device(String token, String model, String os) {
        this.token = token;
        this.model = model;
        this.os = os;
    }

    public void updateUser(User user) {
        if (this.user != null) {
            this.user.getDevices().remove(this);
        }

        this.user = user;

        if (user != null) {
            user.getDevices().add(this);
        }
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
