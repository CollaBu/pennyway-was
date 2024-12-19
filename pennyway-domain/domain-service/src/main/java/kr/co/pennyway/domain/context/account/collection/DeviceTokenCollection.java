package kr.co.pennyway.domain.context.account.collection;

import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.user.domain.User;

public class DeviceTokenCollection {
    public DeviceToken register(User user, String deviceId, String deviceName, String deviceToken) {
        return DeviceToken.of(deviceToken, deviceId, deviceName, user);
    }
}
