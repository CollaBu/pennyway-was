package kr.co.pennyway.socket.common.security.authenticate;

import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.type.Role;
import lombok.Builder;
import lombok.Getter;

import java.security.Principal;
import java.time.LocalDateTime;

@Getter
public class UserPrincipal implements Principal {
    private final Long userId;
    private String name;
    private String username;
    private Role role;
    private boolean isChatNotify;
    private LocalDateTime expiresAt;
    private String deviceId;
    private String deviceName;

    @Builder
    private UserPrincipal(Long userId, String name, String username, Role role, boolean isChatNotify, LocalDateTime expiresAt, String deviceId, String deviceName) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.role = role;
        this.isChatNotify = isChatNotify;
        this.expiresAt = expiresAt;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
    }

    public static UserPrincipal of(User user, LocalDateTime expiresAt, String deviceId, String deviceName) {
        return UserPrincipal.builder()
                .userId(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .role(user.getRole())
                .isChatNotify(user.getNotifySetting().isChatNotify())
                .expiresAt(expiresAt)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .build();
    }

    public void updateExpiresAt(LocalDateTime expiresAt) {
        if (expiresAt.isBefore(this.expiresAt)) {
            throw new IllegalArgumentException("만료 시간을 줄일 수 없습니다.");
        }

        this.expiresAt = expiresAt;
    }

    // Principal이 getName으로 사용자를 식별하는 메서드로 구현되어 있음.
    @Override
    public String getName() {
        return userId.toString();
    }

    // name 필드를 조회하기 위한 메서드
    public String getDefaultName() {
        return name;
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode() * 31;
        return result + username.hashCode() * 31;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UserPrincipal that = (UserPrincipal) obj;
        return userId.equals(that.userId);
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role + '\'' +
                ", isChatNotify=" + isChatNotify + '\'' +
                ", expiresAt=" + expiresAt + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                '}';
    }
}