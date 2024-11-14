package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.api.apis.users.dto.DeviceTokenDto;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.user.domain.User;

public enum DeviceTokenFixture {
    INIT("originToken", "AAA-BBBB-CC-DDDD-EE-FFF", "iPhone 15 Pro"),
    CHANGED_TOKEN("newToken", "AAA-BBBB-CC-DDDD-EE-FFF", "iPhone 15 Pro"),
    ;

    private final String token;
    private final String deviceId;
    private final String deviceName;

    DeviceTokenFixture(String token, String deviceId, String deviceName) {
        this.token = token;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
    }

    public DeviceToken toDevice(User user) {
        return DeviceToken.of(token, deviceId, deviceName, user);
    }

    public DeviceTokenDto.RegisterReq toRegisterReq() {
        return new DeviceTokenDto.RegisterReq(token, deviceId, deviceName);
    }
}
