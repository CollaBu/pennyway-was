package kr.co.pennyway.api.apis.users.mapper;

import kr.co.pennyway.api.apis.users.dto.DeviceTokenDto;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;

@Mapper
public class DeviceTokenMapper {
    public static DeviceTokenDto.RegisterRes toRegisterRes(DeviceToken deviceToken) {
        return DeviceTokenDto.RegisterRes.of(deviceToken.getId(), deviceToken.getToken());
    }
}
