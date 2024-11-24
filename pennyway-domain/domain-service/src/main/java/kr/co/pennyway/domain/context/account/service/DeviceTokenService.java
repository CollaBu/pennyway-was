package kr.co.pennyway.domain.context.account.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.service.DeviceTokenRdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class DeviceTokenService {
    private final DeviceTokenRdbService deviceTokenRdbService;

    @Transactional
    public DeviceToken createDeviceToken(DeviceToken deviceToken) {
        return deviceTokenRdbService.createDevice(deviceToken);
    }

    /**
     * @return 비활성화된 디바이스 토큰 정보를 포함합니다.
     */
    @Transactional(readOnly = true)
    public Optional<DeviceToken> readDeviceTokenByUserIdAndToken(Long userId, String token) {
        return deviceTokenRdbService.readDeviceByUserIdAndToken(userId, token);
    }

    /**
     * @return 비활성화된 디바이스 토큰 정보를 포함합니다.
     */
    @Transactional(readOnly = true)
    public List<DeviceToken> readAllByUserId(Long userId) {
        return deviceTokenRdbService.readAllByUserId(userId);
    }

    @Transactional
    public void deleteDeviceTokensByUserId(Long userId) {
        deviceTokenRdbService.deleteDevicesByUserIdInQuery(userId);
    }
}
