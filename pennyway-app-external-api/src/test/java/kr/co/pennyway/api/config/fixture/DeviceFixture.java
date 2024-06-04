package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.domain.domains.device.domain.Device;
import kr.co.pennyway.domain.domains.user.domain.User;

public enum DeviceFixture {
    INIT("originToken"),
    CHANGED_TOKEN("newToken");

    private final String token;

    DeviceFixture(String token) {
        this.token = token;
    }

    public Device toDevice(User user) {
        return Device.of(token, user);
    }

    public DeviceDto.RegisterReq toRegisterReq() {
        return new DeviceDto.RegisterReq(token);
    }
}
