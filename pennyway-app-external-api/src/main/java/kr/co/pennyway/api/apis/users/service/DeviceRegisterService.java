package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.domain.domains.device.domain.Device;
import kr.co.pennyway.domain.domains.device.exception.DeviceErrorCode;
import kr.co.pennyway.domain.domains.device.exception.DeviceErrorException;
import kr.co.pennyway.domain.domains.device.service.DeviceService;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceRegisterService {
    private final DeviceService deviceService;

    @Transactional
    public Device createOrUpdateDevice(User user, DeviceDto.RegisterReq request) {
        Optional<Device> device = deviceService.readDeviceByUserIdAndToken(user.getId(), request.originToken());

        if (request.isInitRequest() && device.isEmpty()) {
            return createDevice(user, request);
        }

        Device originDevice = getDeviceOrThrow(device);

        return updateDeviceToken(originDevice, request.newToken());
    }

    private Device createDevice(User user, DeviceDto.RegisterReq request) {
        Device newDevice = request.toEntity(user);
        log.debug("신규 디바이스 등록 {}", newDevice);

        return deviceService.createDevice(newDevice);
    }

    /**
     * 사용자 ID와 토큰으로 디바이스 정보를 조회한다.
     *
     * @throws DeviceErrorException 사용자 id와 originToken과 매칭되는 디바이스 정보가 없는 경우
     */
    private Device getDeviceOrThrow(Optional<Device> device) {
        return device.orElseThrow(() -> new DeviceErrorException(DeviceErrorCode.NOT_FOUND_DEVICE));
    }

    /**
     * 디바이스 토큰을 newToken으로 갱신하고, 만약 비활성화 토큰이라면 활성화 상태로 되돌린다.
     */
    private Device updateDeviceToken(Device device, String newToken) {
        log.debug("디바이스 토큰 갱신: {} -> {}", device.getToken(), newToken);

        device.updateToken(newToken);

        return device;
    }
}
