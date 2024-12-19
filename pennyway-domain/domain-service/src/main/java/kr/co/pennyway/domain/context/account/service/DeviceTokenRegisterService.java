package kr.co.pennyway.domain.context.account.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.context.account.collection.DeviceTokenRegisterCollection;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.service.DeviceTokenRdbService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserRdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class DeviceTokenRegisterService {
    private final UserRdbService userRdbService;
    private final DeviceTokenRdbService deviceTokenRdbService;

    /**
     * 사용자의 디바이스 토큰을 생성하거나 갱신한다.
     *
     * @param userId      사용자 식별자
     * @param deviceId    디바이스 식별자
     * @param deviceName  디바이스 이름
     * @param deviceToken 디바이스 토큰
     * @return {@link DeviceToken} 사용자의 기기로 등록된 Device 정보
     */
    @Transactional
    public DeviceToken execute(Long userId, String deviceId, String deviceName, String deviceToken) {
        Optional<User> user = userRdbService.readUser(userId);
        Optional<DeviceToken> existingDeviceToken = deviceTokenRdbService.readDeviceByToken(deviceToken);
        List<DeviceToken> userDeviceTokens = deviceTokenRdbService.readByUserIdAndDeviceId(userId, deviceId);

        DeviceToken newDeviceToken = new DeviceTokenRegisterCollection(
                existingDeviceToken.orElse(null),
                userDeviceTokens
        ).register(user.orElse(null), deviceId, deviceName, deviceToken);

        return deviceTokenRdbService.createDevice(newDeviceToken);
    }
}
