package kr.co.pennyway.domain.domains.device.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorCode;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorException;
import kr.co.pennyway.domain.domains.device.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class DeviceTokenRdbService {
    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * @throws DeviceTokenErrorException 중복된 디바이스 토큰이 이미 존재하는 경우
     */
    @Transactional
    public DeviceToken createDevice(DeviceToken deviceToken) {
        try {
            return deviceTokenRepository.save(deviceToken);
        } catch (DataIntegrityViolationException e) {
            log.error("DeviceToken 등록 중 중복 에러가 발생했습니다. deviceToken: {}", deviceToken);

            throw new DeviceTokenErrorException(DeviceTokenErrorCode.DUPLICATED_DEVICE_TOKEN);
        }
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
