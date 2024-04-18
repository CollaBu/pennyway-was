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

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceRegisterService {
    private final DeviceService deviceService;

    @Transactional
    public Device createOrUpdateDevice(User user, DeviceDto.RegisterReq request) {
        if (request.isInitRequest()) {
            return createDevice(user, request);
        } else {
            return updateExistingDevice(user, request);
        }
    }

    private Device createDevice(User user, DeviceDto.RegisterReq request) {
        log.info("신규 디바이스 등록: 사용자 {} - model {} - os {}", user, request.model(), request.os());
        Device newDevice = request.toEntity(user);
        return deviceService.createDevice(newDevice);
    }

    /**
     * 기존에 등록된 사용자의 디바이스 토큰을 갱신한다.
     */
    private Device updateExistingDevice(User user, DeviceDto.RegisterReq request) {
        Device device = readDeviceOrThrow(user.getId(), request.originToken());

        if (!isMatchOriginDeviceInfo(device, request)) {
            log.warn("유효하지 않은 요청: 사용자 {} - model {} - os {}", user, request.model(), request.os());
            throw new DeviceErrorException(DeviceErrorCode.NOT_MATCH_DEVICE);
        }

        log.info("디바이스 토큰 갱신: 사용자 {} - model {} - os {}", user, request.model(), request.os());
        return updateDeviceToken(device, request.newToken());
    }

    /**
     * 사용자 ID와 토큰으로 디바이스 정보를 조회한다.
     *
     * @throws DeviceErrorException 사용자 id와 originToken과 매칭되는 디바이스 정보가 없는 경우
     */
    private Device readDeviceOrThrow(Long userId, String token) {
        return deviceService.readDeviceByUserIdAndToken(userId, token)
                .orElseThrow(() -> new DeviceErrorException(DeviceErrorCode.NOT_FOUND_DEVICE));
    }

    /**
     * 요청한 디바이스 정보가 기존 디바이스 정보와 일치하는지 확인한다.
     */
    private boolean isMatchOriginDeviceInfo(Device device, DeviceDto.RegisterReq request) {
        return device.getOs().equals(request.os()) && device.getModel().equals(request.model());
    }

    /**
     * 디바이스 토큰을 newToken으로 갱신하고, 만약 비활성화 토큰이라면 활성화 상태로 되돌린다.
     */
    private Device updateDeviceToken(Device device, String newToken) {
        log.debug("디바이스 토큰 갱신: {} -> {}", device.getToken(), newToken);

        device.updateToken(newToken);

        if (device.getActivated().equals(Boolean.FALSE)) {
            device.activate();
        }

        return device;
    }
}
