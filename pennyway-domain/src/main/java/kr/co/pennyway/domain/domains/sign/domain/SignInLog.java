package kr.co.pennyway.domain.domains.sign.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.converter.IpAddressHeaderConverter;
import kr.co.pennyway.domain.domains.sign.type.IpAddressHeader;
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
    @Convert(converter = IpAddressHeaderConverter.class)
    private IpAddressHeader ipAddressHeader;
    private String appVersion;
    private String deviceModel;
    private String os;

    @Builder
    public SignInLog(LocalDateTime signedAt, Long userId, String ipAddress, IpAddressHeader ipAddressHeader, String appVersion, String deviceModel, String os) {
        this.signedAt = signedAt;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.ipAddressHeader = ipAddressHeader;
        this.appVersion = appVersion;
        this.deviceModel = deviceModel;
        this.os = os;
    }
}
