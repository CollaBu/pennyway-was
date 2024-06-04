package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.domain.domains.device.domain.Device;
import kr.co.pennyway.domain.domains.user.domain.User;

public enum DeviceFixture {
    INIT("originToken", "originToken"),
    CHANGED_TOKEN("originToken", "newToken");

    private final String originToken;
    private final String newToken;

    DeviceFixture(String originToken, String newToken) {
        this.originToken = originToken;
        this.newToken = newToken;
    }

    public Device toDevice(User user) {
        return Device.of(originToken, user);
    }

    public DeviceDto.RegisterReq toRegisterReq() {
        return new DeviceDto.RegisterReq(originToken, newToken);
    }
}
