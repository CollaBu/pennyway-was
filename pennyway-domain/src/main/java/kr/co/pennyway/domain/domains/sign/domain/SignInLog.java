package kr.co.pennyway.domain.domains.sign.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Table(name = "sign_in_log")
@IdClass(SignInLogId.class)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class SignInLog {
    @Id
    private LocalDateTime signedAt;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String ipAddress;
    private String deviceModel;
    private String os;

    @Builder
    private SignInLog(LocalDateTime signedAt, Long id, Long userId, String ipAddress, String deviceModel, String os) {
        this.signedAt = signedAt;
        this.id = id;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.deviceModel = deviceModel;
        this.os = os;
    }
}
