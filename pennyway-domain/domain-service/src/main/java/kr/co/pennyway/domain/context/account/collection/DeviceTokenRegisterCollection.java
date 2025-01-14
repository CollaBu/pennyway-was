package kr.co.pennyway.domain.context.account.collection;

import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorCode;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorException;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.List;

@Slf4j
public class DeviceTokenRegisterCollection {
    private final List<DeviceToken> userDeviceTokens;
    private DeviceToken deviceToken;

    public DeviceTokenRegisterCollection() {
        this.deviceToken = null;
        this.userDeviceTokens = List.of();
    }

    public DeviceTokenRegisterCollection(DeviceToken deviceToken) {
        this.deviceToken = deviceToken;
        this.userDeviceTokens = List.of();
    }

    /**
     * @param userDeviceTokens :
     */
    public DeviceTokenRegisterCollection(DeviceToken deviceToken, List<DeviceToken> userDeviceTokens) {
        this.deviceToken = deviceToken;
        this.userDeviceTokens = userDeviceTokens;
    }

    /**
     * 사용자의 디바이스 토큰을 생성하거나 갱신한다.
     *
     * <pre>
     * [비즈니스 규칙]
     * - 같은 {userId, deviceId}에 대해 새로운 토큰이 발급될 수 있지만, 활성화된 토큰은 하나여야 합니다.
     * - {deviceId, token} 조합은 시스템 전체에서 유일해야 합니다.
     * - device token이 이미 등록된 경우, 소유자 정보를 갱신하고 마지막 로그인 시간을 갱신한다.
     * - device token이 등록되지 않은 경우, 새로운 device token을 생성한다.
     * </pre>
     *
     * @param owner      User :   사용자 정보
     * @param deviceId   String: 디바이스 식별자
     * @param deviceName String: 디바이스 이름
     * @param token      String: 디바이스 토큰
     * @return {@link DeviceToken} 사용자의 기기로 등록된 Device 정보
     * @throws UserErrorException        {@link UserErrorCode#NOT_FOUND} : 사용자 파라미터가 null인 경우
     * @throws DeviceTokenErrorException {@link DeviceTokenErrorCode#DUPLICATED_DEVICE_TOKEN} : 이미 등록된 활성화 디바이스 토큰의 deviceId와 다른 deviceId가 들어온 경우
     */
    public DeviceToken register(@NonNull User owner, @NonNull String deviceId, @NonNull String deviceName, @NonNull String token) {
        DeviceToken existingDeviceToken = this.getDeviceTokenByToken(token);

        return (existingDeviceToken != null)
                ? this.updateDevice(owner, deviceId, existingDeviceToken)
                : this.createDevice(owner, deviceId, deviceName, token);
    }

    private DeviceToken getDeviceTokenByToken(String token) {
        if (this.deviceToken != null && this.deviceToken.getToken().equals(token)) {
            return this.deviceToken;
        }

        return null;
    }

    private DeviceToken updateDevice(User user, String deviceId, DeviceToken originalDeviceToken) {
        if (isDuplicatedDeviceToken(deviceId, originalDeviceToken)) {
            log.error("활성화된 토큰을 다른 디바이스에서 사용 중입니다.");
            throw new DeviceTokenErrorException(DeviceTokenErrorCode.DUPLICATED_DEVICE_TOKEN);
        }

        originalDeviceToken.handleOwner(user, deviceId);
        log.info("디바이스 토큰이 갱신되었습니다. deviceId: {}, token: {}", deviceId, originalDeviceToken.getToken());

        return originalDeviceToken;
    }

    private boolean isDuplicatedDeviceToken(String deviceId, DeviceToken originalDeviceToken) {
        return !originalDeviceToken.getDeviceId().equals(deviceId) && originalDeviceToken.isActivated();
    }

    private DeviceToken createDevice(User user, String deviceId, String deviceName, String token) {
        this.deviceToken = DeviceToken.of(token, deviceId, deviceName, user);
        log.info("새로운 디바이스 토큰이 생성되었습니다. deviceId: {}, token: {}", deviceId, token);

        deactivateExistingTokens();

        return this.deviceToken;
    }

    /**
     * 특정 사용자의 디바이스에 대한 기존 활성 토큰들을 비활성화합니다.
     * 새로운 토큰 등록 시 호출되어 하나의 디바이스에 하나의 활성 토큰만 존재하도록 보장합니다.
     */
    private void deactivateExistingTokens() {
        userDeviceTokens.stream()
                .filter(token -> token.getDeviceId().equals(deviceToken.getDeviceId()))
                .filter(DeviceToken::isActivated)
                .forEach(DeviceToken::deactivate);
    }
}
