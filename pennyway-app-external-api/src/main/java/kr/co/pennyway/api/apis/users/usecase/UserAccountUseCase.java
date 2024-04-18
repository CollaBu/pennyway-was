package kr.co.pennyway.api.apis.users.usecase;

import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.device.domain.Device;
import kr.co.pennyway.domain.domains.device.exception.DeviceErrorCode;
import kr.co.pennyway.domain.domains.device.exception.DeviceErrorException;
import kr.co.pennyway.domain.domains.device.service.DeviceService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UserAccountUseCase {
    private final UserService userService;
    private final DeviceService deviceService;

    @Transactional
    public DeviceDto.RegisterRes registerDevice(Long userId, DeviceDto.RegisterReq request) {
        User user = userService.readUser(userId).orElseThrow(
                () -> new UserErrorException(UserErrorCode.NOT_FOUND)
        );
        Long deviceId;

        // 디바이스 토큰이 같은 경우, 신규 등록이라 판단하고 등록
        if (request.isSameToken()) {
            log.info("신규 디바이스 등록: 사용자 {} - model {} - os {}", userId, request.model(), request.os());
            Device device = deviceService.createDevice(request.toEntity(user));
            return DeviceDto.RegisterRes.of(device.getId(), request.newToken());
        }

        Optional<Device> device = deviceService.readDeviceByUserIdAndToken(userId, request.originToken());

        if (device.isPresent()) { // 기존 디바이스 토큰이 존재하는 경우, 토큰 갱신
            Device oldDevice = device.get();

            if (!oldDevice.getOs().equals(request.os()) || !oldDevice.getModel().equals(request.model())) {
                log.warn("유효하지 않은 요청: 사용자 {} - model {} - os {}", userId, request.model(), request.os());
                // 어떻게?
                throw new DeviceErrorException(DeviceErrorCode.NOT_MATCH_DEVICE);
            }

            log.info("디바이스 토큰 갱신: 사용자 {} - model {} - os {}", userId, request.model(), request.os());
            oldDevice.updateToken(request.newToken());

            if (oldDevice.getActivated().equals(Boolean.FALSE)) {
                oldDevice.activate();
            }

            deviceId = oldDevice.getId();
        } else { // 기존 디바이스 토큰이 존재하지 않는 경우, 신규 등록
            log.warn("{}번 사용자의 요청 디바이스 토큰을 찾을 수 없습니다. 요청 토큰 : {}", userId, request.originToken());
            throw new DeviceErrorException(DeviceErrorCode.NOT_FOUND_DEVICE);
        }

        return DeviceDto.RegisterRes.of(deviceId, request.newToken());
    }

    @Transactional
    public void unregisterDevice(Long userId, Long deviceId) {

    }
}
