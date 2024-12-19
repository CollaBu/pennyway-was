package kr.co.pennyway.domain.context.account.collection;

import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorCode;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorException;
import kr.co.pennyway.domain.domains.user.domain.User;

public class DeviceTokenCollection {
    private final DeviceToken deviceToken;

    public DeviceTokenCollection() {
        this.deviceToken = null;
    }

    public DeviceTokenCollection(DeviceToken deviceToken) {
        this.deviceToken = deviceToken;
    }

    public DeviceToken register(User user, String deviceId, String deviceName, String token) {
        DeviceToken existingDeviceToken = this.getDeviceTokenByToken(token);

        if (existingDeviceToken != null) {
            if (!existingDeviceToken.getDeviceId().equals(deviceId) && existingDeviceToken.isActivated()) {
                throw new DeviceTokenErrorException(DeviceTokenErrorCode.DUPLICATED_DEVICE_TOKEN);
            }

            existingDeviceToken.handleOwner(user, deviceId);
            return existingDeviceToken;
        }

        return DeviceToken.of(token, deviceId, deviceName, user);
    }

    private DeviceToken getDeviceTokenByToken(String token) {
        if (this.deviceToken != null && this.deviceToken.getToken().equals(token)) {
            return this.deviceToken;
        }

        return null;
    }
}
