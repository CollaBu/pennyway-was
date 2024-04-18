package kr.co.pennyway.domain.domains.device.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.device.domain.Device;
import kr.co.pennyway.domain.domains.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;

    @Transactional
    public Device createDevice(Device device) {
        return deviceRepository.save(device);
    }

    @Transactional
    public Optional<Device> readDeviceByUserIdAndToken(Long userId, String token) {
        return deviceRepository.findByUser_IdAndToken(userId, token);
    }

    @Transactional(readOnly = true)
    public List<Device> readDevicesByUserId(Long userId) {
        return deviceRepository.findAllByUser_Id(userId);
    }

    @Transactional
    public void deleteDevice(Long deviceId) {
        deviceRepository.deleteById(deviceId);
    }

    @Transactional
    public void deleteDevice(Device device) {
        deviceRepository.delete(device);
    }
}
