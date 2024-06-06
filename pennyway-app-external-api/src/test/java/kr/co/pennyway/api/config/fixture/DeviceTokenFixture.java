package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.api.apis.users.dto.DeviceTokenDto;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.user.domain.User;

public enum DeviceTokenFixture {
    INIT("originToken"),
    CHANGED_TOKEN("newToken");

    private final String token;

    DeviceTokenFixture(String token) {
        this.token = token;
    }

    public DeviceToken toDevice(User user) {
        return DeviceToken.of(token, user);
    }

    public DeviceTokenDto.RegisterReq toRegisterReq() {
        return new DeviceTokenDto.RegisterReq(token);
    }
}
