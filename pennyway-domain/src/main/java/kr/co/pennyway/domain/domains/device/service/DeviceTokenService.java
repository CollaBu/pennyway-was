package kr.co.pennyway.domain.domains.device.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class DeviceTokenService {
    private final DeviceTokenRepository deviceTokenRepository;

    @Transactional
    public DeviceToken createDevice(DeviceToken deviceToken) {
        return deviceTokenRepository.save(deviceToken);
    }

    @Transactional
    public Optional<DeviceToken> readDeviceByUserIdAndToken(Long userId, String token) {
        return deviceTokenRepository.findByUser_IdAndToken(userId, token);
    }

    @Transactional
    public void deleteDevice(DeviceToken deviceToken) {
        deviceTokenRepository.delete(deviceToken);
    }

    @Transactional
    public void deleteDevicesByUserIdInQuery(Long userId) {
        deviceTokenRepository.deleteAllByUserIdInQuery(userId);
    }
}
