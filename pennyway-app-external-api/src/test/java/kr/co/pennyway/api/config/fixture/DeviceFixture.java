package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.domain.domains.device.domain.Device;
import kr.co.pennyway.domain.domains.user.domain.User;

public enum DeviceFixture {
    INIT("originToken", "originToken", "modelA", "Windows 11"),
    ORIGIN_DEVICE("originToken", "originToken", "modelA", "Windows 11"),
    ONLY_TOKEN_CHANGED("originToken", "newToken", "modelA", "Windows 11"),
    ONLY_MODEL_AND_OS_CHANGED("originToken", "originToken", "modelB", "Windows 11"),
    ALL_CHANGED("originToken", "newToken", "modelB", "Mac OS X");

    private final String originToken;
    private final String newToken;
    private final String model;
    private final String os;

    DeviceFixture(String originToken, String newToken, String model, String os) {
        this.originToken = originToken;
        this.newToken = newToken;
        this.model = model;
        this.os = os;
    }

    public Device toDevice(User user) {
        return Device.of(originToken, user);
    }

    public DeviceDto.RegisterReq toRegisterReq() {
        return new DeviceDto.RegisterReq(originToken, newToken);
    }
}
