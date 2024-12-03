package kr.co.pennyway.domain.domains.device.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class DeviceTokenRdbService {
    private final DeviceTokenRepository deviceTokenRepository;

    @Transactional
    public DeviceToken createDevice(DeviceToken deviceToken) {
        return deviceTokenRepository.save(deviceToken);
    }

    /**
     * @return 비활성화된 디바이스 토큰 정보를 포함합니다.
     */
    @Transactional(readOnly = true)
    public Optional<DeviceToken> readDeviceByUserIdAndToken(Long userId, String token) {
        return deviceTokenRepository.findByUser_IdAndToken(userId, token);
    }

    @Transactional(readOnly = true)
    public Optional<DeviceToken> readByDeviceIdAndToken(String deviceId, String token) {
        return deviceTokenRepository.findByDeviceIdAndToken(deviceId, token);
    }

    @Transactional(readOnly = true)
    public List<DeviceToken> readByUserIdAndDeviceId(Long userId, String deviceId) {
        return deviceTokenRepository.findAllByUser_IdAndDeviceId(userId, deviceId);
    }

    /**
     * @return 비활성화된 디바이스 토큰 정보를 포함합니다.
     */
    @Transactional(readOnly = true)
    public List<DeviceToken> readAllByUserId(Long userId) {
        return deviceTokenRepository.findAllByUser_Id(userId);
    }

    @Transactional
    public void deleteDevicesByUserIdInQuery(Long userId) {
        deviceTokenRepository.deleteAllByUserIdInQuery(userId);
    }
}
