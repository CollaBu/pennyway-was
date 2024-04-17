package kr.co.pennyway.api.apis.users.usecase;

import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.device.domain.Device;
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
    public Long registerDevice(Long userId, DeviceDto.RegisterReq request) {
        User user = userService.readUser(userId).orElseThrow(
                () -> new UserErrorException(UserErrorCode.NOT_FOUND)
        );

        // 디바이스 토큰이 같은 경우, 신규 등록이라 판단하고 등록
        if (request.isSameToken()) {
            Device device = deviceService.createDevice(request.toEntity(user));
            return device.getId();
        }

        Optional<Device> device = deviceService.readDeviceByUserIdAndToken(userId, request.originToken());

        if (device.isPresent()) { // 기존 디바이스 토큰이 존재하는 경우, 토큰 갱신
            device.get().updateToken(request.newToken());
            return device.get().getId();
        } else { // 기존 디바이스 토큰이 존재하지 않는 경우, 신규 등록
            Device newDevice = request.toEntity(user);
            deviceService.createDevice(newDevice);
            return newDevice.getId();
        }
    }

}
