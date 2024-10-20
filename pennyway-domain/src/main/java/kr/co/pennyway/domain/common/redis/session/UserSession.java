package kr.co.pennyway.domain.common.redis.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Convert;
import kr.co.pennyway.domain.common.converter.UserStatusConverter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class UserSession implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String deviceId;
    private String deviceName;
    @Convert(converter = UserStatusConverter.class)
    private UserStatus status;
    private Long currentChatRoomId;
    private LocalDateTime lastActiveAt;
    @JsonIgnore
    private int hashCode;

    @JsonCreator
    private UserSession(
            @JsonProperty("deviceId") String deviceId,
            @JsonProperty("deviceName") String deviceName,
            @JsonProperty("status") UserStatus status,
            @JsonProperty("currentChatRoomId") Long currentChatRoomId,
            @JsonProperty("lastActiveAt") LocalDateTime lastActiveAt
    ) {
        validate(deviceId, deviceName, status, lastActiveAt);

        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.status = status;
        this.currentChatRoomId = currentChatRoomId;
        this.lastActiveAt = lastActiveAt;
    }

    /**
     * 새로운 사용자 세션을 생성한다.
     * 사용자의 상태는 ACTIVE_APP이며, 채팅방 관련 뷰룰 보고 있지 않음을 전제로 한다.
     * 마지막 활동 시간은 현재 시간으로 설정된다.
     */
    public static UserSession of(String deviceId, String deviceName) {
        return new UserSession(deviceId, deviceName, UserStatus.ACTIVE_APP, -1L, LocalDateTime.now());
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public UserStatus getStatus() {
        return status;
    }

    /**
     * 사용자가 보고 있는 채팅방 ID를 반환한다.
     *
     * @return 사용자가 보고 있는 채팅방 ID. 채팅방을 보고 있지 않을 경우 -1을 반환한다.
     */
    public Long getCurrentChatRoomId() {
        if (!this.status.equals(UserStatus.ACTIVE_CHAT_ROOM)) {
            return -1L;
        }

        return currentChatRoomId;
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void updateStatus(UserStatus status) {
        validate(deviceId, deviceName, status, lastActiveAt);

        this.status = status;
        updateLastActiveAt();
    }

    public void updateStatus(UserStatus status, Long currentChatRoomId) {
        validate(deviceId, deviceName, status, lastActiveAt);

        if (status.equals(UserStatus.ACTIVE_CHAT_ROOM) && (currentChatRoomId == null || currentChatRoomId <= 0)) {
            throw new IllegalArgumentException("채팅방 ID는 null 혹은 0을 포함한 음수를 허용하지 않습니다.");
        }

        this.status = status;
        this.currentChatRoomId = currentChatRoomId;
        updateLastActiveAt();
    }

    /**
     * 사용자의 마지막 활동 시간을 현재 시간으로 갱신한다.
     */
    public void updateLastActiveAt() {
        this.lastActiveAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSession that = (UserSession) o;
        return deviceId.equals(that.deviceId) && status == that.status && Objects.equals(currentChatRoomId, that.currentChatRoomId) && lastActiveAt.equals(that.lastActiveAt);
    }

    @Override
    public int hashCode() {
        if (hashCode != -1) {
            return hashCode;
        }

        int result = deviceId.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + lastActiveAt.hashCode();
        return hashCode = result;
    }

    @Serial
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        // 가변 요소를 방어적으로 복사
        deviceId = String.copyValueOf(deviceId.toCharArray());
        deviceName = String.copyValueOf(deviceName.toCharArray());
        status = UserStatus.valueOf(status.name());
        lastActiveAt = LocalDateTime.of(lastActiveAt.toLocalDate(), lastActiveAt.toLocalTime());
        currentChatRoomId = this.currentChatRoomId == null ? -1L : Long.valueOf(currentChatRoomId);

        // 불변식을 만족하는지 검사한다.
        validate(deviceId, deviceName, status, lastActiveAt);
    }

    private void validate(String deviceId, String deviceName, UserStatus status, LocalDateTime lastActiveAt) {
        if (deviceId == null) {
            throw new IllegalStateException("deviceId는 null일 수 없습니다.");
        }
        if (deviceName == null) {
            throw new IllegalStateException("deviceName은 null일 수 없습니다.");
        }
        if (status == null) {
            throw new IllegalStateException("status는 null일 수 없습니다.");
        }
        if (lastActiveAt == null) {
            throw new IllegalStateException("lastActiveAt은 null일 수 없습니다.");
        }
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", status='" + status + '\'' +
                ", currentChatRoomId='" + currentChatRoomId + '\'' +
                ", lastActiveAt='" + lastActiveAt + '\'' +
                '}';
    }
}
